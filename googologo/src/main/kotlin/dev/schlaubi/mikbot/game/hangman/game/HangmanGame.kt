package dev.schlaubi.mikbot.game.hangman.game

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.stickerId
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.utils.waitFor
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.SingleWinnerGame
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.google_emotes.*
import dev.schlaubi.mikbot.game.hangman.HangmanModule
import dev.schlaubi.mikbot.games.translations.HangmanTranslations
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.minutes

class HangmanGame(
    lastWinner: UserBehavior?,
    host: UserBehavior,
    module: HangmanModule,
    override val welcomeMessage: Message,
    override val thread: ThreadChannelBehavior,
    override val translationsProvider: TranslationsProvider,
) : SingleWinnerGame<HangmanPlayer>(host, module.asType),
    Rematchable<HangmanPlayer, HangmanGame>,
    AutoJoinableGame<HangmanPlayer> {
    override val rematchThreadName: String = "googologo-rematch"
    private val wordOwner = lastWinner ?: host
    override val playerRange: IntRange = 2..Int.MAX_VALUE
    private val gameCompleter by lazy { CompletableDeferred<Unit>() }
    private var state: GameState = GameState.WaitingForWord

    override fun obtainNewPlayer(user: User): HangmanPlayer = HangmanPlayer(user)

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: dev.kord.common.Locale?,
    ): HangmanPlayer = HangmanPlayer(user)

    private suspend fun retrieveWord(): String? {
        val wordOwner = players.first { it.user == wordOwner }
        players.remove(wordOwner)
        leftPlayers.add(wordOwner)
        val message =
            thread.createMessage(translate(HangmanTranslations.Game.Ui.WordSelection.ask, wordOwner.user.mention))

        val wordEvent = kord.waitFor<MessageCreateEvent>(1.minutes.inWholeMilliseconds) {
            // Check if message is from wordOwner
            this.message.channel.asChannelOfOrNull<DmChannel>()?.recipientIds?.contains(wordOwner.user.id) == true
        }
        message.delete()

        if (wordEvent == null) {
            thread.createMessage {
                content = translate(HangmanTranslations.Game.Ui.WordSelection.timeout)
            }
            softEnd()
            return null
        }

        val word = wordEvent.message.content
        if (word.length !in 3..102) {
            wordEvent.message.reply {
                content = translate(HangmanTranslations.Hangman.Game.wrongWord)
            }

            thread.createMessage {
                content = translate(HangmanTranslations.Hangman.Game.WrongWord.public)
            }
            softEnd()
            return null
        }

        wordEvent.message.reply {
            content = translate(HangmanTranslations.Hangman.wordAccepted, word, thread.mention)
        }

        return word
    }

    private suspend fun startGame(scope: CoroutineScope, word: String) {
        val listener =
            kord.events.filterIsInstance<MessageCreateEvent>().filter { it.message.channelId == thread.id }.filter {
                val user = it.message.author ?: return@filter false
                players.any { event -> event.user == user }
            }.onEach(::onNewGuess).launchIn(scope)

        val state = GameState.Guessing(
            listener, word
        )
        this@HangmanGame.state = state

        welcomeMessage.edit {
            embeds = mutableListOf(state.toEmbed(this@HangmanGame))
        }
        thread.createMessage(translate(HangmanTranslations.Game.started))
    }

    private suspend fun onNewGuess(event: MessageCreateEvent) = coroutineScope {
        launch { event.message.delete("Hangman input") }
        (state as? GameState.Guessing)?.mutex?.withLock {
            val guessingState = state as? GameState.Guessing ?: return@withLock
            val char = event.message.content.uppercase(Locale.ENGLISH).singleOrNull()
            state = if (char == null) {
                if (event.message.content.equals(guessingState.word, ignoreCase = true)) {
                    GameState.Done(players.first { it.user == event.message.author }, guessingState.word)
                } else {
                    guessingState.copy(blackList = guessingState.blackList + event.message.content)
                }
            } else {
                guessingState.copy(chars = guessingState.chars + char)
            }

            state.takeIfIsInstance<GameState.Guessing> {
                val guessedChars = chars.map { it.lowercase() }
                when {
                    guessedChars.containsAll(
                        word
                            .asSequence()
                            .map { it.lowercase() }
                            .filterNot { it.isBlank() } // you do not have to guess white spaces
                            .distinct()
                            .toList()
                    ) ->
                        state =
                            GameState.Done(players.first { it.user == event.message.author }, guessingState.word)

                    (wrongChars.size + blackList.size) >= maxTries ->
                        state =
                            GameState.Done(leftPlayers.first { it.user == wordOwner }, guessingState.word)

                    else -> welcomeMessage.edit {
                        embeds = mutableListOf(toEmbed(this@HangmanGame))
                    }
                }
            }

            state.takeIfIsInstance<GameState.Done> {
                this@HangmanGame.launch {
                    state.close()
                    this@HangmanGame.winner = winner
                    gameCompleter.complete(Unit)
                }
            }
        }
    }

    private fun softEnd() = kord.launch { doEnd() }

    override suspend fun runGame() = coroutineScope {
        welcomeMessage.edit { components = mutableListOf() }
        val word = retrieveWord() ?: return@coroutineScope
        startGame(this@HangmanGame, word)

        if (state is GameState.Guessing) {
            // wait for game to finish
            gameCompleter.await()
            cancel() // kill orphans
        }
    }

    override suspend fun end() {
        state.close()
        if (state is GameState.Done && winner!!.user == wordOwner) {
            welcomeMessage.reply {
                stickerId(Snowflake(861039079151763486))
            }
        }
    }

    override suspend fun EmbedBuilder.endEmbed(messageModifyBuilder: MessageModifyBuilder) {
        if (!running) return

        val word = (state as? GameState.HasWord)?.word ?: return
        if (winner!!.user == wordOwner) {
            description = translate(HangmanTranslations.Game.Ui.lost, word)
        } else {
            field {
                name = translate(HangmanTranslations.Hangman.word)
                value = word
            }
        }
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): HangmanGame {
        val game = HangmanGame(
            winner!!.user, host, module as HangmanModule, welcomeMessage, thread, translationsProvider
        )
        val actualPlayers = players + HangmanPlayer(wordOwner)
        game.players.addAll(actualPlayers)
        return game
    }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: HangmanPlayer) {
        event.interaction.deferEphemeralMessageUpdate()
    }

    companion object {
        val googologo = listOf(capitalG, redO, yellowO, smallG, redO, l, yellowO, smallG, redO)
        val maxTries = googologo.size
    }
}

sealed interface GameState {
    suspend fun close() = Unit

    object WaitingForWord : GameState

    interface HasWord {
        val word: String
    }

    data class Guessing(
        val listener: Job,
        override val word: String,
        val chars: Set<Char> = emptySet(),
        val blackList: Set<String> = emptySet(),
        val mutex: Mutex = Mutex(),
    ) : GameState, HasWord {
        val wrongChars: Set<Char> = chars.filter { it !in word.uppercase(Locale.ENGLISH) }.toSet()
        override suspend fun close() {
            listener.cancel()
        }
    }

    data class Done(val winner: HangmanPlayer, override val word: String) : GameState, HasWord
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : GameState> GameState.takeIfIsInstance(block: T.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (this is T) {
        block()
    }
}

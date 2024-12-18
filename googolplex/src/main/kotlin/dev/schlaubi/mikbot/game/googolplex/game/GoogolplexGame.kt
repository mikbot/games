package dev.schlaubi.mikbot.game.googolplex.game

import dev.kord.common.Locale
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.toKey
import dev.schlaubi.mikbot.game.api.*
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.google_emotes.googleLogoColor
import dev.schlaubi.mikbot.game.google_emotes.googleLogoWhite
import dev.schlaubi.mikbot.games.translations.GoogolplexTranslations
import dev.schlaubi.mikbot.plugin.api.util.discordError

private val correctColor get() = generateSequence { googleLogoWhite }
private val correctPosition get() = generateSequence { googleLogoColor }

class GoogolplexGame(
    val size: Int,
    private val maxTries: Int,
    private val lastWinner: GoogolplexPlayer?,
    host: UserBehavior,
    module: GameModule<GoogolplexPlayer, AbstractGame<GoogolplexPlayer>>,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider
) : SingleWinnerGame<GoogolplexPlayer>(host, module), Rematchable<GoogolplexPlayer, GoogolplexGame>,
    ControlledGame<GoogolplexPlayer> {
    override val rematchThreadName: String = "googolplex-rematch"
    override val playerRange: IntRange = 2 until 3
    lateinit var correctSequence: List<ReactionEmoji>

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?
    ): GoogolplexPlayer = GoogolplexPlayer(user, loading, ack, userLocale, this)

    override suspend fun runGame() {
        val startingUser = lastWinner?.user ?: host
        val startingPlayer = startingUser.gamePlayer
        val guessingPlayer = (players - startingPlayer).first()

        correctSequence = startingPlayer.awaitInitialSequence(this)
        startingPlayer.controls.edit {
            components = mutableListOf()
        }
        val existingGuesses = mutableListOf<String>()

        var done = false
        var tries = 0
        while (!done) {
            fun last() = existingGuesses.takeLast(25)
            val lastGuess = guessingPlayer.awaitSequence(
                size,
                translate(guessingPlayer, GoogolplexTranslations.Googolplex.Controls.requestGuess, size)
            ) { _, current ->
                interaction.deferEphemeralMessageUpdate()
                updateGameStateMessage(last(), current)
            }
            existingGuesses += lastGuess.buildGuessUI(correctSequence)
            updateGameStateMessage(last())
            done = lastGuess == correctSequence || ++tries > maxTries
        }
        winner = if (tries <= maxTries) {
            guessingPlayer // only the guessing player can win
        } else {
            startingPlayer
        }
    }

    private suspend fun updateGameStateMessage(
        last: List<String>,
        current: List<ReactionEmoji>? = null
    ) {
        welcomeMessage.edit {
            addResendControlsButton()

            val description = buildString {
                appendLine(translate(GoogolplexTranslations.Game.Io.guessesHeading))

                if (last.isNotEmpty()) {
                    last.forEach {
                        appendLine(it)
                    }

                    if (current != null) {
                        appendLine()
                    }
                }

                current?.forEach {
                    append(it.mention)
                }
            }
            embed(fun EmbedBuilder.() {
                this.description = description

                field {
                    name = translate(GoogolplexTranslations.Game.Ui.Legend.title)
                    value = translate(GoogolplexTranslations.Game.Ui.legend, googleLogoWhite.mention, googleLogoColor.mention)
                }
            })
        }
    }

    private fun List<ReactionEmoji>.buildGuessUI(correctSequence: List<ReactionEmoji>) = buildString {
        this@buildGuessUI.forEach {
            append(it.mention)
        }
        val hints = buildHintList(correctSequence)
        if (hints.isNotEmpty()) {
            append(" | ")
            buildHintList(correctSequence).forEach {
                append(it)
            }
        }
    }

    private fun List<ReactionEmoji>.buildHintList(correctSequence: List<ReactionEmoji>): List<String> {
        val (correctPositions, wrongPositions) = correctSequence
            .withIndex()
            .partition { (index, color) -> this[index] == color }

        val correctColors = wrongPositions.count { (_, color) ->
            color in this
        }

        return (correctColor.take(correctColors) + correctPosition.take(correctPositions.size))
            .map { it.mention }
            .toList()
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): GoogolplexGame {
        val game = GoogolplexGame(
            size,
            maxTries,
            winner!!,
            host,
            module,
            thread,
            welcomeMessage,
            translationsProvider
        )
        if (!askForRematch(
                thread,
                game
            )
        ) {
            discordError("Game could not restart".toKey())
        }

        return game
    }
}

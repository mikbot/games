package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.common.Locale
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.schlaubi.mikbot.game.api.ControlledPlayer
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.mikbot.game.uno.game.ui.translationKey
import dev.schlaubi.mikbot.game.uno.game.ui.welcomeMessage
import dev.schlaubi.mikbot.games.translations.GameApiTranslations
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.mikbot.plugin.api.util.deleteAfterwards
import dev.schlaubi.uno.Game
import dev.schlaubi.uno.Player
import dev.schlaubi.uno.cards.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.mikbot.game.api.Player as GamePlayer

internal val unoInteractionTimeout = 30.seconds.inWholeMilliseconds

const val drawCardButton = "draw_card"
const val sayUnoButton = "say_uno"
const val skipButton = "skip"
const val allCardsButton = "request_all_cards"
const val challengeWildCard = "challenge_wild_card"

abstract class DiscordUnoPlayer(
    override val user: UserBehavior,
    var response: EphemeralMessageInteractionResponseBehavior,
    override var controls: FollowupMessage,
    override val game: DiscordUnoGame,
    override val discordLocale: Locale?,
) : Player(), GamePlayer, ControlledPlayer {
    override val ack: MessageInteractionResponseBehavior
        get() = response

    private var myTurn = false
    internal var drawn = false
    var turns: Int = 0
        private set

    override fun onSkip() {
        game.launch {
            if (game.flashMode) {
                response.createEphemeralFollowup {
                    content = translate(UnoTranslations.Uno.Flash.skipped)
                }
            } else {
                response.createEphemeralFollowup {
                    content = translate(UnoTranslations.Uno.Controls.skipped)
                }
            }
        }
    }

    override fun onUnoBluff() {
        game.launch {
            response.createEphemeralFollowup {

                content = translate(UnoTranslations.Uno.General.saidOnAsBluff)
            }
        }
    }

    override fun onCardsDrawn(amount: Int) {
        if (amount >= 1) {
            game.launch {
                response.createEphemeralFollowup {

                    content = translate(UnoTranslations.Uno.Controls.drawn, amount)
                }
            }
        }
    }

    override fun refreshCards() {
        game.launch {
            updateControls(false)
        }
    }

    override fun onWin(place: Int) {
        game.kord.launch {
            game.thread.createMessage("${user.mention} finished the game!")
            controls.edit {
                components = mutableListOf()
                content = translate(UnoTranslations.Uno.Controls.won)
            }
        }
    }

    override fun forgotUno(game: Game<*>) {
        this.game.kord.launch {
            response.createEphemeralFollowup {

                content = translate(UnoTranslations.Uno.General.forgotUno)
            }
        }
    }

    override fun onVisibleCards(otherPlayer: Player) {
        game.launch {
            val cards = ack.createEphemeralFollowup {

                val cards = otherPlayer.deck.map { translate(it.translationKey) }.joinToString(", ")
                content = translate(
                    UnoTranslations.Uno.Controls.ViewCards.show,
                    (otherPlayer as DiscordUnoPlayer).user.mention, cards
                )
            }
            delay(2.3.seconds)
            cards.edit { content = translate(UnoTranslations.Uno.Controls.ViewCards.over) }
        }
    }

    suspend fun turn(secondRun: Boolean = false, drewCards:Boolean = false) {
        val cardCount = deck.size // Store to check whether cards were drawn
        myTurn = true
        // Draw card stacking logic
        if (game.game.drawCardSum >= 1 || (!game.allowDrawCardStacking && game.game.topCard is DrawingCard)) {
            if (game.allowDrawCardStacking && deck.any { (it as? DrawingCard)?.canStackWith(game.game.topCard) == true }) {
                // if you can stack, ask to stack
                val card: DrawingCard? = pickDrawingCardToStack()
                if (card == null) {
                    // or draw
                    doDraw()
                } else {
                    playCard(game.game, card)
                    return endTurn()
                }
            } else {
                // if you can't stack or stacking is disabled, auto-draw
                doDraw()
            }
        }

        val topCard = game.game.topCard
        val isSkippingCard = topCard is SkipCard || topCard is ReverseCard

        // Don't update if it wasn't another players turn
        updateControls((game.lastPlayer != this && !secondRun) || isSkippingCard || drewCards || saidUno)
        val cantPlay by lazy { deck.none { it.canBePlayedOn(game.game.topCard) } }
        if (drawn) {
            if (cantPlay) {
                // disabled until less-annoying solution is found
//                ack.followUpEphemeral {
//                    content = translate("uno.controls.auto_skipped")
//                }
                return endTurn() // auto-skip if drawn and can't play
            }
        } else if (!secondRun && game.game.drawCardSum >= 1 && cantPlay && !game.game.canBeChallenged) {
            doDraw() // auto draw if there is no other option
            return turn(secondRun = true)
        }

        val cardName = awaitResponse { controls }

        myTurn = false // Prevent clicking again
        if (cardName != null) {
            if (normalPlay(cardName)) {
                val playable = deck.filter { it.canBePlayedOn(game.game.topCard) }
                // Auto-play with force play
                if (game.forcePlay && playable.size == 1) {
                    uno()
                    updateControls(false)
                    normalPlay("play_card_${deck.indexOf(playable.first())}")
                    return
                }
                // if the action calls for a new turn repeat
                return turn(secondRun = true, drewCards = deck.size > cardCount)
            }
        } else {
            aiPlay()
        }

        endTurn()
    }

    private suspend fun endTurn() {
        updateControls(false)
        drawn = false
        turns++
    }

    private suspend fun normalPlay(cardName: String): Boolean {
        when (cardName) {
            challengeWildCard -> {
                game.game.challenge(this)
                return false
            }
            drawCardButton -> {
                doDraw()
                return true
            }
            sayUnoButton -> {
                playUno()
                return true
            }
            allCardsButton -> {
                val cards = deck.map { translate(it.translationKey) }.joinToString(", ")
                response.createEphemeralFollowup {

                    content = cards.substring(0, 2000.coerceAtMost(cards.length))
                }
                return true
            }
            skipButton -> return false
            else -> {
                val cardId = cardName.substringAfter("play_card_").toInt()
                val card = deck[cardId]
                if (card is DiscardAllCardsCard) {
                    uno() // you cannot say uno unless having two cards, so we just auto-uno here
                }

                if (card is SlapCard) {
                    updateControls(false) // Disable controls for player during slap turn
                }

                if (card is AbstractWildCard && deck.size != 1) { // ignore pick on last card
                    val color = pickWildCardColor()
                    playCard(game.game, card, color)
                } else if ((card as? SimpleCard)?.number == 7 && game.useSpecial7and0) {
                    val player = pickPlayer()
                    playCard(game.game, card, player)
                } else {
                    playCard(game.game, card)
                }

                return false
            }
        }
    }

    private fun doDraw() {
        drawn = true
        onCardsDrawn(game.game.drawCardSum)
        draw(game.game)
    }

    private suspend fun playUno() {
        uno()
        val message = game.thread.createMessage("${user.mention} just said UNO!")

        game.kord.launch {
            runCatching {
                // Ignore thread is archived errors
                message.deleteAfterwards(15.seconds)
            }
        }
    }

    private suspend fun aiPlay() {
        // Probably most horrible AI ever but just don't go afk
        val card = deck.firstOrNull { it.canBePlayedOn(game.game.topCard) }
        if (card != null) {
            if (deck.size == 2) {
                playUno()
            }

            playCard(game.game, card)
        } else {
            draw(game.game)
        }
    }

    override suspend fun resendControls(ack: EphemeralMessageInteractionResponseBehavior) =
        resendControlsInternally(null, response = ack)

    @OptIn(KordUnsafe::class)
    suspend fun resendControlsInternally(
        event: ComponentInteractionCreateEvent? = null,
        justLoading: Boolean = false,
        response: EphemeralMessageInteractionResponseBehavior? = null,
    ) {
        val ack = response ?: event?.interaction?.deferEphemeralResponseUnsafe() ?: this.response
        controls = ack.createEphemeralFollowup {

            content = translate(UnoTranslations.Uno.Controls.loading)
        }
        if (!justLoading) {
            editControls(myTurn)
        }
    }

    abstract suspend fun updateControls(active: Boolean, initial: Boolean = false)

    open suspend fun MessageModifyBuilder.updateControlsMessage(initial: Boolean = false) = Unit

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscordUnoPlayer) return false

        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        return user.hashCode()
    }

    override fun onSlap(context: SlapContext) {
        game.launch {
            openSlapCardUI(context)
        }
    }

    override fun onSlapEnd() {
        game.launch {
            updateControls(false)
            response.createPublicFollowup {

                translate(UnoTranslations.Game.SlapSession.lost)
            }
        }
    }

    override fun toString(): String = user.toString()
}

class DesktopPlayer(
    user: UserBehavior,
    response: EphemeralMessageInteractionResponseBehavior,
    controls: FollowupMessage,
    game: DiscordUnoGame,
    discordLocale: Locale?,
) : DiscordUnoPlayer(user, response, controls, game, discordLocale) {
    override suspend fun updateControls(active: Boolean, initial: Boolean) = editControls(active)
}

class MobilePlayer(
    user: UserBehavior,
    response: EphemeralMessageInteractionResponseBehavior,
    controls: FollowupMessage,
    game: DiscordUnoGame,
    discordLocale: Locale?,
) : DiscordUnoPlayer(user, response, controls, game, discordLocale) {
    // Add the game message to the controls
    override suspend fun MessageModifyBuilder.updateControlsMessage(initial: Boolean) {
        // Only add this text, to playable messages
        if (components?.isNotEmpty() == true && !initial) {
            embed(fun EmbedBuilder.() {
                welcomeMessage(game)

                footer {
                    text = translate(UnoTranslations.Uno.Controls.Mobile.notice)
                }
            })
        }
    }

    override suspend fun updateControls(active: Boolean, initial: Boolean) {
        // If it is your turn, we'll resend your controls
        if (active) {
            coroutineScope {
                launch {
                    controls.edit {
                        content = game.translate(this@MobilePlayer, GameApiTranslations.Game.Controls.reset)
                        embeds = mutableListOf()
                        components = mutableListOf()
                    }
                }
                resendControlsInternally()
            }
        } else {
            // if not we'll just edit
            editControls(false, initial)
        }
    }
}

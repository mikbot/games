package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.modify.FollowupMessageModifyBuilder
import dev.kordex.core.i18n.toKey
import dev.schlaubi.mikbot.game.uno.game.ui.buttonStyle
import dev.schlaubi.mikbot.game.uno.game.ui.emoji
import dev.schlaubi.mikbot.game.uno.game.ui.translationKey
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.uno.cards.Card

private suspend fun DiscordUnoPlayer.cardsTitle(active: Boolean, cardSize: Int): String {
    val (key, replacements) = if (active) {
        if (cardSize != deck.size) {
            "uno.controls.active.hidden.head" to arrayOf(deck.size - cardSize)
        } else {
            "uno.controls.active.head" to emptyArray()
        }
    } else {
        "uno.controls.inactive.head" to emptyArray()
    }

    return translate(key.toKey(), *replacements)
}

suspend fun DiscordUnoPlayer.editControls(active: Boolean, initial: Boolean = false) {
    controls.edit {
        val availableCards = displayableCards()
        val cards = availableCards
            .sortedBy { (card) -> card } // sort by card
            .chunked(5) // Only 5 buttons per action row

        content = cardsTitle(active, availableCards.size)
        addCards(cards, this@editControls, active)
        addControlButtons(
            this@editControls, active,
            availableCards.size != deck.size
        )
        updateControlsMessage(initial)
    }
}

private suspend fun FollowupMessageModifyBuilder.addControlButtons(
    discordUnoPlayer: DiscordUnoPlayer,
    active: Boolean,
    cardsHidden: Boolean
) {
    actionRow {
        if (!discordUnoPlayer.drawn) {
            interactionButton(ButtonStyle.Danger, drawCardButton) {
                label = discordUnoPlayer.translate(UnoTranslations.Uno.Actions.drawCard)
                disabled = !active
            }
        } else {
            interactionButton(ButtonStyle.Danger, skipButton) {
                label = discordUnoPlayer.translate(UnoTranslations.Uno.Actions.skip)
                disabled = !active || !discordUnoPlayer.drawn || discordUnoPlayer.game.forcePlay
            }
        }

        if (cardsHidden) {
            interactionButton(ButtonStyle.Danger, allCardsButton) {
                label = discordUnoPlayer.translate(UnoTranslations.Uno.Actions.requestAllCards)
                disabled = !active
            }
        }

        if (discordUnoPlayer.deck.size <= 2) {
            interactionButton(
                if (discordUnoPlayer.saidUno) ButtonStyle.Success else ButtonStyle.Primary,
                sayUnoButton
            ) {
                label = discordUnoPlayer.translate(UnoTranslations.Uno.Actions.sayUno)
                disabled = !active || discordUnoPlayer.deck.size <= 1 || discordUnoPlayer.saidUno
            }
        }

        if (discordUnoPlayer.game.game.canBeChallenged) {
            interactionButton(ButtonStyle.Danger, challengeWildCard) {
                label = discordUnoPlayer.translate(UnoTranslations.Uno.Actions.challengeWildCard)
                disabled = !active
            }
        }
    }
}

private suspend fun FollowupMessageModifyBuilder.addCards(
    cards: List<List<IndexedValue<Card>>>,
    discordUnoPlayer: DiscordUnoPlayer,
    active: Boolean
) {
    cards.forEach {
        actionRow {
            it.forEach { (index, card) ->
                interactionButton(card.buttonStyle, "play_card_$index") {
                    emoji = DiscordPartialEmoji(id = Snowflake(card.emoji), name = "1")
                    label = discordUnoPlayer.translate(card.translationKey)
                    disabled = !active || !card.canBePlayedOn(discordUnoPlayer.game.game.topCard)
                }
            }
        }
    }
}

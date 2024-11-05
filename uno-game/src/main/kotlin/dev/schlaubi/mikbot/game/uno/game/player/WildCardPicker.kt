package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.rest.builder.message.actionRow
import dev.schlaubi.mikbot.game.uno.game.ui.buttonStyle
import dev.schlaubi.mikbot.game.uno.game.ui.localizedName
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.uno.UnoColor

suspend fun DiscordUnoPlayer.pickWildCardColor(): UnoColor {
    val key = awaitResponse(UnoTranslations.Uno.Controls.WildCard.done) {
        content = translate(UnoTranslations.Uno.Controls.WildCard.pickColor)

        actionRow {
            UnoColor.entries.forEach { color ->
                interactionButton(color.buttonStyle, color.name) {
                    label = color.localizedName
                }
            }
        }
    } ?: return UnoColor.BLUE

    return UnoColor.valueOf(key)
}

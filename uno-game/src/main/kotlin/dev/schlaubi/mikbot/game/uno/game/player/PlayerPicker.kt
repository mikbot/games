package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.actionRow
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.uno.Player

suspend fun DiscordUnoPlayer.pickPlayer(): Player {
    val availablePlayers = game.players - this
    if (availablePlayers.size == 1) return availablePlayers.first()

    val playerId = awaitResponse(UnoTranslations.Uno.Controls.SwitchCards.done) {
        content = translate(UnoTranslations.Uno.Controls.SwitchCards.pickPlayer)

        availablePlayers.chunked(5).forEach {
            actionRow {
                it.forEach { player ->
                    interactionButton(ButtonStyle.Primary, player.user.id.toString()) {
                        label = player.user.asUser().tag
                    }
                }
            }
        }
    } ?: return availablePlayers.first()

    return availablePlayers.first { it.user.id == Snowflake(playerId) }
}

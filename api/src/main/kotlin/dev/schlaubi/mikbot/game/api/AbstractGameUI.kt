package dev.schlaubi.mikbot.game.api

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.schlaubi.mikbot.games.translations.GameApiTranslations

const val leaveGameButton = "leave_game"
const val joinGameButton = "join_game"
const val startGameButton = "start_game"

fun ActionRowBuilder.leaveButton(text: String) = interactionButton(ButtonStyle.Danger, leaveGameButton) {
    label = text
}

suspend fun MessageModifyBuilder.gameUI(game: AbstractGame<*>) {
    actionRow {
        if (!game.running) {
            interactionButton(ButtonStyle.Success, joinGameButton) {
                label = game.translate(key = GameApiTranslations.Game.Ui.join)
            }

            interactionButton(ButtonStyle.Primary, startGameButton) {
                label = game.translate(GameApiTranslations.Game.Ui.start)
            }
        }

        leaveButton(game.translate(key = GameApiTranslations.Game.Ui.leave))
        linkButton("https://discord.com/channels/${game.thread.guildId}/${game.thread.id}") {
            label = game.translate(key = GameApiTranslations.Game.Ui.fullscreen)
        }
    }
}

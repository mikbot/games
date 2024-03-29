package dev.schlaubi.mikbot.game.api

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.modify.MessageModifyBuilder

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
                label = game.translateInternally(key = "game.ui.join")
            }

            interactionButton(ButtonStyle.Primary, startGameButton) {
                label = game.translateInternally(key = "game.ui.start")
            }
        }

        leaveButton(game.translateInternally(key = "game.ui.leave"))
        linkButton("https://discord.com/channels/${game.thread.guildId}/${game.thread.id}") {
            label = game.translateInternally(key = "game.ui.fullscreen")
        }
    }
}

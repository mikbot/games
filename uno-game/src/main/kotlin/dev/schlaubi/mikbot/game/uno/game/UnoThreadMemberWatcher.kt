package dev.schlaubi.mikbot.game.uno.game

import dev.kord.core.behavior.interaction.followup.edit
import dev.schlaubi.mikbot.game.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.mikbot.game.uno.game.player.translate
import dev.schlaubi.mikbot.games.translations.UnoTranslations

suspend fun DiscordUnoGame.kickPlayer(player: DiscordUnoPlayer) {
    runCatching {
        player.controls.edit {
            components = mutableListOf()
            content = player.translate(UnoTranslations.Uno.Controls.left)
        }
    }

    if (!running) return
    game.removePlayer(player)

    // Cancel turn for current player if it is the leaving player or,
    // there are no players left (end game)
    if (currentPlayer == player || game.players.size <= 1) {
        // leaving confuses the player sequence and lets the left player play again
        game.nextPlayer()
        currentTurn?.cancel()
    }
}

package dev.schlaubi.mikbot.game.hangman

import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.hangman.game.HangmanGame
import dev.schlaubi.mikbot.game.hangman.game.HangmanPlayer
import dev.schlaubi.mikbot.games.translations.HangmanTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

class HangmanModule(context: PluginContext) : GameModule<HangmanPlayer, HangmanGame>(context) {
    override val name: String = "googologo"
    override val gameStats: CoroutineCollection<UserGameStats> = HangmanDatabase.stats

    @OptIn(PrivilegedIntent::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMessages)
        intents.add(Intent.MessageContent)

        startGameCommand(
            HangmanTranslations.Hangman.Game.title,
            "googologo",
            { message, thread ->
                HangmanGame(null, user, this@HangmanModule, message, thread, get())
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand(HangmanTranslations.Commands.Hangman.Leaderboard.title)
    }
}

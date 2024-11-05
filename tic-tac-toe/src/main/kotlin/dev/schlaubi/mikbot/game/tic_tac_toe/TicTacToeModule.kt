package dev.schlaubi.mikbot.game.tic_tac_toe

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.defaultingEnumChoice
import dev.kordex.core.i18n.EMPTY_KEY
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.tic_tac_toe.game.GameSize
import dev.schlaubi.mikbot.game.tic_tac_toe.game.TicTacToeGame
import dev.schlaubi.mikbot.game.tic_tac_toe.game.TicTacToePlayer
import dev.schlaubi.mikbot.games.translations.TicTacToeTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

class StartGameArguments : Arguments() {
    val gameSize by defaultingEnumChoice<GameSize> {
        name = TicTacToeTranslations.Commands.Start.Arguments.Size.name
        description = TicTacToeTranslations.Commands.Start.Arguments.Size.description

        defaultValue = GameSize.`3_BY_3`
        typeName = EMPTY_KEY
    }
}

class TicTacToeModule(context: PluginContext) : GameModule<TicTacToePlayer, TicTacToeGame>(context) {
    override val name: String = "tic-tac-toe"
    override val gameStats: CoroutineCollection<UserGameStats> = TicTacToeDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand(TicTacToeTranslations.TicTacToe.lobby, "tic-tac-toe", ::StartGameArguments, { message, thread ->
            TicTacToeGame(
                null,
                arguments.gameSize,
                thread,
                message,
                get(),
                user,
                asType
            )
        })
        stopGameCommand()
        leaderboardCommand(TicTacToeTranslations.TicTacToe.leaderboard)
        profileCommand()
    }
}

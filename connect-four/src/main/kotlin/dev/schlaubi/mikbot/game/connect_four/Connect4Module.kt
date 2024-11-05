package dev.schlaubi.mikbot.game.connect_four

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.kordex.core.i18n.TranslationsProvider
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Game
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Player
import dev.schlaubi.mikbot.games.translations.ConnectFourTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.math.min

private const val bundle = "connect_four"

class Connect4Arguments : Arguments() {
    val height by defaultingInt {
        name = ConnectFourTranslations.Commands.Start.Arguments.Height.name
        description = ConnectFourTranslations.Commands.Start.Arguments.Height.description
        defaultValue = 6

        validate {
            if (value > 20) {
                discordError(ConnectFourTranslations.Commands.Start.tooHighHeight)
            } else if (value <= 0) {
                discordError(ConnectFourTranslations.Commands.Start.tooLowHeight)
            }
        }
    }

    val width by defaultingInt {
        name = ConnectFourTranslations.Commands.Start.Arguments.Width.name
        description = ConnectFourTranslations.Commands.Start.Arguments.Width.description
        validate {
            if (value > 9) {
                discordError(ConnectFourTranslations.Commands.Start.tooHighWidth)
            } else if (value <= 0) {
                discordError(ConnectFourTranslations.Commands.Start.tooLowWidth)
            }
        }
        defaultValue = 7
    }

    val connect by defaultingInt {
        name = ConnectFourTranslations.Commands.Start.Arguments.Connect.name
        description = ConnectFourTranslations.Commands.Start.Arguments.Connect.description
        defaultValue = 4
    }
}

class Connect4Module(context: PluginContext) : GameModule<Connect4Player, AbstractGame<Connect4Player>>(context) {
    override val name: String = "connect4"
    override val gameStats: CoroutineCollection<UserGameStats> = Connect4Database.stats

    override suspend fun gameSetup() {
        startGameCommand(
            ConnectFourTranslations.Game.title, "connect4", ::Connect4Arguments,
            {
                if (min(arguments.width, arguments.height) < arguments.connect) {
                    discordError(ConnectFourTranslations.Commands.Start.impossibleConnect)
                }
            },
            { _, message, thread ->
                if (arguments.connect == 1) {
                    OnePlayerConnect4Game(
                        arguments.height,
                        arguments.width,
                        thread,
                        message,
                        get<TranslationsProvider>(),
                        user,
                        this@Connect4Module
                    ).apply {
                        players.add(Connect4Player(user, Connect4.Player.RED))
                    }
                } else {
                    Connect4Game(
                        arguments.height,
                        arguments.width,
                        arguments.connect,
                        thread,
                        message,
                        get<TranslationsProvider>(),
                        user,
                        this@Connect4Module
                    ).apply {
                        players.add(Connect4Player(user, possibleTypes.poll()))
                    }
                }
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand(ConnectFourTranslations.Game.leaderboard)
    }
}

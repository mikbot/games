package dev.schlaubi.mikbot.game.googolplex

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.googolplex.game.GoogolplexGame
import dev.schlaubi.mikbot.game.googolplex.game.GoogolplexPlayer
import dev.schlaubi.mikbot.plugin.api.PluginContext
import org.litote.kmongo.coroutine.CoroutineCollection

class StartGameArguments : Arguments() {
    val size by defaultingInt {
        name = "length"
        description = "commands.start.arguments.length.description"

        defaultValue = 4
    }

    val maxTries by defaultingInt {
        name = "max-tries"
        description = "commands.start.arguments.max_tries.description"

        defaultValue = 10
    }
}

class GoogolplexModule(context: PluginContext) : GameModule<GoogolplexPlayer, GoogolplexGame>(context) {
    override val name: String = "googolplex"
    override val bundle: String = "googolplex"
    override val gameStats: CoroutineCollection<UserGameStats> = GoogolplexDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand(
            "googolplex.lobby",
            "googolplex",
            ::StartGameArguments,
            { message, thread ->
                GoogolplexGame(
                    arguments.size,
                    arguments.maxTries,
                    null,
                    user,
                    asType,
                    thread,
                    message,
                    translationsProvider
                )
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("googolplex.stats.title")
    }
}

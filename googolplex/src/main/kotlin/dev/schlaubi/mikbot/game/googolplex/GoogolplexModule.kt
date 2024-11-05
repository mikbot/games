package dev.schlaubi.mikbot.game.googolplex

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.googolplex.game.GoogolplexGame
import dev.schlaubi.mikbot.game.googolplex.game.GoogolplexPlayer
import dev.schlaubi.mikbot.games.translations.GoogolplexTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

class StartGameArguments : Arguments() {
    val size by defaultingInt {
        name = GoogolplexTranslations.Commands.Start.Arguments.Length.name
        description = GoogolplexTranslations.Commands.Start.Arguments.Length.description

        defaultValue = 4
    }

    val maxTries by defaultingInt {
        name = GoogolplexTranslations.Commands.Start.Arguments.MaxTries.name
        description = GoogolplexTranslations.Commands.Start.Arguments.MaxTries.description

        defaultValue = 10
    }
}

class GoogolplexModule(context: PluginContext) : GameModule<GoogolplexPlayer, GoogolplexGame>(context) {
    override val name: String = "googolplex"
    override val gameStats: CoroutineCollection<UserGameStats> = GoogolplexDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand(
            GoogolplexTranslations.Googolplex.lobby,
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
                    get()
                )
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand(GoogolplexTranslations.Googolplex.Stats.title)
    }
}

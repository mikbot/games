package dev.schlaubi.mikbot.game.uno

import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.mikbot.game.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

class UnoArguments : Arguments() {
    val extreme by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.Extreme.name
        description = UnoTranslations.Commands.Start.Arguments.Extreme.description
        defaultValue = false
    }
    val flash by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.Flash.name
        description = UnoTranslations.Commands.Start.Arguments.Flash.description
        defaultValue = false
    }
    val dropIns by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.DropIns.name
        description = UnoTranslations.Commands.Start.Arguments.DropIns.description
        defaultValue = false
    }
    val drawUntilPlayable by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.DrawUntilPlayable.name
        description = UnoTranslations.Commands.Start.Arguments.DrawUntilPlayable.description
        defaultValue = false
    }
    val forcePlay by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.ForcePlay.name
        description = UnoTranslations.Commands.Start.Arguments.ForcePlay.description
        defaultValue = false
    }

    val enableDrawCardStacking by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.CardStacking.name
        description = UnoTranslations.Commands.Start.Arguments.CardStacking.description
        defaultValue = true
    }

    val stackAllDrawingCards by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.StackAllDrawingCards.name
        description = UnoTranslations.Commands.Start.Arguments.StackAllDrawingCards.description
        defaultValue = false
    }

    val enableBluffing by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.Bluffing.name
        description = UnoTranslations.Commands.Start.Arguments.Bluffing.description
        defaultValue = false
    }

    val useSpecial7and0 by defaultingBoolean {
        name = UnoTranslations.Commands.Start.Arguments.SevenAndZero.name
        description = UnoTranslations.Commands.Start.Arguments.SevenAndZero.description
        defaultValue = false
    }
}

class UnoModule(context: PluginContext) : GameModule<DiscordUnoPlayer, DiscordUnoGame>(context) {
    override val name: String = "uno"
    override val commandName: Key = "uno".toKey()

    override val gameStats: CoroutineCollection<UserGameStats> = database.getCollection("uno_stats")

    @OptIn(PrivilegedIntent::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMembers)
        intents.add(Intent.GuildPresences)

        startGameCommand(
            UnoTranslations.Uno.Game.title,
            "uno-game",
            ::UnoArguments,
            {
                if (arguments.flash && arguments.useSpecial7and0) {
                    discordError(UnoTranslations.Commands.Uno.StartGame.Special7And0.Incompatible.flash)
                }
            },
            { _, welcomeMessage, thread ->
                DiscordUnoGame(
                    user, this@UnoModule, welcomeMessage, thread, get(),
                    arguments.extreme, arguments.flash, arguments.dropIns, arguments.drawUntilPlayable,
                    arguments.forcePlay, arguments.enableDrawCardStacking, arguments.stackAllDrawingCards,
                    arguments.enableBluffing, arguments.useSpecial7and0
                )
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand(UnoTranslations.Uno.Game.title)
        bluffingCommand()
    }
}

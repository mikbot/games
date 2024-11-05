package dev.schlaubi.mikbot.game.api.module.commands

import dev.kordex.core.checks.isInThread
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.games.translations.GameApiTranslations
import dev.schlaubi.mikbot.plugin.api.util.ifPassing
import dev.schlaubi.mikbot.plugin.api.util.translate

/**
 * Adds a /stop command to this [GameModule].
 */
fun GameModule<*, *>.stopGameCommand() = ephemeralSubCommand {
    name = GameApiTranslations.Commands.Stop.name
    description = GameApiTranslations.Commands.Stop.description

    check {
        isInThread()

        ifPassing {
            failIf(GameApiTranslations.Commands.StopGame.notRunning) {
                findGame(event.interaction.channelId) == null
            }
        }
    }

    action {
        val game = findGame(channel.id)!!
        if (user != game.host) {
            respond {
                content = translate(GameApiTranslations.Commands.StopGame.permissionDenied)
            }
            return@action
        }

        respond {
            content = translate(GameApiTranslations.Commands.StopGame.success)
        }

        game.doEnd(true)
    }
}

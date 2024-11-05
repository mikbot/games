package dev.schlaubi.mikbot.game.uno

import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.mikbot.plugin.api.util.translate

fun UnoModule.bluffingCommand() = ephemeralSubCommand {
    name = UnoTranslations.Commands.Bluffing.name
    description = UnoTranslations.Commands.Uno.Bluffing.description

    action {
        respond {
            content = translate(UnoTranslations.Commands.Uno.Bluffing.description)
        }
    }
}

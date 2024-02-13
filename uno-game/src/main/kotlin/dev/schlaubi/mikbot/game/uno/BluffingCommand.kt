package dev.schlaubi.mikbot.game.uno

fun UnoModule.bluffingCommand() = ephemeralSubCommand {
    name = "bluffing"
    description = "commands.bluffing.description"

    action {
        respond {
            content = translate("commands.uno.bluffing.description")
        }
    }
}

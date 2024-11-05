package dev.schlaubi.mikbot.game.tic_tac_toe

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class TicTacToePlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::TicTacToeModule)
    }
}

package dev.schlaubi.mikbot.game.connect_four

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class Connect4Plugin(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::Connect4Module)
    }
}

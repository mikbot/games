package dev.schlaubi.mikbot.game.trivia

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class TriviaPlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::TriviaModule)
    }
}

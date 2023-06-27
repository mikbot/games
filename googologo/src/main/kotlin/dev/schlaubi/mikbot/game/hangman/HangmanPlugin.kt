package dev.schlaubi.mikbot.game.hangman

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class HangmanPlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::HangmanModule)
    }
}

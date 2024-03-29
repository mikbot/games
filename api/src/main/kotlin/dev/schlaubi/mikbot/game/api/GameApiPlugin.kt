package dev.schlaubi.mikbot.game.api

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginContext

/**
 * This plugin is just here to load shared classes between game plugins.
 */
@PluginMain
class GameApiPlugin(context: PluginContext) : Plugin(context)

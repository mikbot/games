package dev.schlaubi.mikbot.game.trivia

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object Config : EnvironmentConfig("") {
    @OptIn(ExperimentalEncodingApi::class)
    val GOOGLE_TRANSLATE_KEY by getEnv { Base64.decode(it) }
    val GOOGLE_TRANSLATE_PROJECT_ID by this
    val GOOGLE_TRANSLATE_LOCATION by getEnv("global")
}

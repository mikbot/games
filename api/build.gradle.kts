import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
    kotlin("plugin.serialization")
    `mikbot-publishing`
}

version = apiVersion

dependencies {
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Plugin providing core APIs for all games"
    bundle = "games"
}

import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "3.0.1"

dependencies {
    plugin(projects.api)
    implementation(projects.uno)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Plugin adding functionality to play UNO on Discord"
    bundle = "uno"
}

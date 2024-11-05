import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "4.0.0"

dependencies {
    plugin(projects.api)
    implementation(projects.uno)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Plugin adding functionality to play UNO on Discord"
    bundle = "uno"
}

import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "3.0.1"

dependencies {
    plugin(projects.api)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Probably the most inefficient implementation of Tic Tac Toe, but JVM is fast, so it doesn't matter"
    bundle = "tic_tac_toe"
}

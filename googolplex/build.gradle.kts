import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "2.10.0"

dependencies {
    plugin(projects.api)
    implementation(projects.googleEmotes)
}

mikbotPlugin {
    description = "My version of mastermind"
}

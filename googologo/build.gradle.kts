import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "4.0.1"

dependencies {
    implementation(projects.googleEmotes)
    plugin(projects.api)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Hangman but with family friendly"
    bundle = "hangman"
}

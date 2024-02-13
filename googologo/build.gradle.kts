import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "3.0.0"

dependencies {
    implementation(projects.googleEmotes)
    plugin(projects.api)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Hangman but with family friendly"
    bundle = "hangman"
}

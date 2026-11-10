import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
    kotlin("plugin.serialization")
}

version = "5.2.0"

dependencies {
    plugin(projects.api)
    plugin(libs.mikbot.music)
    plugin(projects.multipleChoiceGame)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Plugin providing Song Quizzes"
    bundle = "song_quiz"
}

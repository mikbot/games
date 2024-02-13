import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
    kotlin("plugin.serialization")
}

version = "4.0.0"

dependencies {
    plugin(projects.api)
    plugin(libs.mikbot.music)
    plugin(projects.multipleChoiceGame)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
        }
    }
}

mikbotPlugin {
    description = "Plugin providing Song Quizzes"
    bundle = "song_quiz"
}

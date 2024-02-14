import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "3.0.1"

dependencies {
    implementation(projects.googleEmotes)
    plugin(projects.api)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Connect four"
    bundle = "connect_four"
}

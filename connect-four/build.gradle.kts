import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
}

version = "4.0.0"

dependencies {
    implementation(projects.googleEmotes)
    plugin(projects.api)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
}

mikbotPlugin {
    description = "Connect four"
    bundle = "connect_four"
}

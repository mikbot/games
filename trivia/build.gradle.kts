import dev.schlaubi.mikbot.gradle.mikbot

plugins {
    mikbot
    kotlin("plugin.serialization")
}

version = "2.13.0"

dependencies {
    plugin(projects.api)
    plugin(projects.multipleChoiceGame)
    optionalPlugin(mikbot(libs.mikbot.gdpr))
    implementation(libs.commons.text)

    // Google Translate
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.translate)
}

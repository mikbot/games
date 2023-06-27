@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import dev.schlaubi.mikbot.gradle.mikbot
import dev.schlaubi.mikbot.gradle.addRepositories

plugins {
    org.jetbrains.kotlin.jvm
    `mikbot-publishing`
}

version = "2.3.0"

// Add mikbot repository
addRepositories()

dependencies {
    compileOnly(mikbot(libs.mikbot.api))
}

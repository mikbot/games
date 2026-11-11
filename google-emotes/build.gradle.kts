@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import dev.schlaubi.mikbot.gradle.mikbot
import dev.schlaubi.mikbot.gradle.addRepositories
import gradle.kotlin.dsl.accessors._3177f1741d83226eb5345f101f351731.java
import gradle.kotlin.dsl.accessors._3177f1741d83226eb5345f101f351731.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    org.jetbrains.kotlin.jvm
    `mikbot-publishing`
}

version = "3.2.1"

// Add mikbot repository
project.addRepositories()

dependencies {
    compileOnly(mikbot(libs.mikbot.api))
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_26
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_26
}


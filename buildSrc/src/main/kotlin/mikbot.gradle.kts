import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    org.jetbrains.kotlin.jvm
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        jvmTarget = JvmTarget.JVM_26
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_26
}


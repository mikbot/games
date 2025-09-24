import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

plugins {
    dev.schlaubi.mikbot.`gradle-plugin`
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"

    repositories {
        mavenCentral()
        maven("https://maven.topi.wtf/releases")
    }
}

subprojects {
    afterEvaluate {
        configure<KotlinBaseExtension> {
            jvmToolchain(24)
        }
    }
}

mikbotPlugin {
    provider = "Mikbot Team"
    license = "MIT"

    i18n {
        classPackage = "dev.schlaubi.mikbot.games.translations"
    }
}

pluginPublishing {
    targetDirectory = rootProject.file("ci-repo")
    projectUrl = "https://github.com/mikbot/utils"
    repositoryUrl = "https://storage.googleapis.com/mikbot-plugins"
}

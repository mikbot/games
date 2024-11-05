import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

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
        configure<KotlinTopLevelExtension> {
            jvmToolchain(22)
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

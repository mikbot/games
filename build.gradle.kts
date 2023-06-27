import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import kotlin.io.path.div

plugins {
    dev.schlaubi.mikbot.`gradle-plugin`
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "dev.schlaubi.tonbrett"

    repositories {
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        configure<KotlinTopLevelExtension> {
            jvmToolchain(19)
        }
    }
}

mikbotPlugin {
    provider = "Mikbot Team"
    license = "MIT"
}

pluginPublishing {
    targetDirectory = rootDir.toPath() / "ci-repo"
    projectUrl = "https://github.com/mikbot/utils"
    repositoryUrl = "https://storage.googleapis.com/mikbot-plugins"
}

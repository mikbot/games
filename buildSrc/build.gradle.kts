plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://releases-repo.kordex.dev")
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.mikbot.gradle.plugin)
    implementation(libs.ksp.plugin)
    implementation("gradle.plugin.com.google.cloud.artifactregistry", "artifactregistry-gradle-plugin", "2.2.1")
}

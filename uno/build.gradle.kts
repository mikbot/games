plugins {
    org.jetbrains.kotlin.jvm
}

version = "3.0.0"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}

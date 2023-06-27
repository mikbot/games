plugins {
    org.jetbrains.kotlin.jvm
}

version = "2.9.0"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}

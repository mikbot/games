import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    org.jetbrains.kotlin.jvm
}

version = "4.0.0"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}


kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_26
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_26
}

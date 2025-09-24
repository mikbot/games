plugins {
    org.jetbrains.kotlin.jvm
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

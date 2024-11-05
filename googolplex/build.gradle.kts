plugins {
    mikbot
}

version = "4.0.0"

dependencies {
    plugin(projects.api)
    implementation(projects.googleEmotes)
}

mikbotPlugin {
    description = "My version of mastermind"
}

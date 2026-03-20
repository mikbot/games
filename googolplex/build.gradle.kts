plugins {
    mikbot
}

version = "4.1.0"

dependencies {
    plugin(projects.api)
    implementation(projects.googleEmotes)
}

mikbotPlugin {
    description = "My version of mastermind"
}

plugins {
    mikbot
}

version = "2.11.0"

dependencies {
    plugin(projects.api)
    implementation(projects.googleEmotes)
}

mikbotPlugin {
    description = "My version of mastermind"
}

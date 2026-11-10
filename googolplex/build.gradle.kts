plugins {
    mikbot
}

version = "4.2.0"

dependencies {
    plugin(projects.api)
    implementation(projects.googleEmotes)
}

mikbotPlugin {
    description = "My version of mastermind"
}

plugins {
    mikbot
    `mikbot-publishing`
}

version = apiVersion

dependencies {
    plugin(projects.api)
}

mikbotPlugin {
    bundle = "multiple_choice"
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "mikbot-games"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":uno",
    ":google-emotes",
    ":api",
    ":multiple-choice-game",
    ":uno-game",
    ":music-quiz",
    ":trivia",
    ":connect-four",
    ":googologo",
    ":googolplex",
    ":tic-tac-toe"
)

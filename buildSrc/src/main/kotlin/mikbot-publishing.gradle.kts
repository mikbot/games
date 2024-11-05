import java.util.*

plugins {
    `maven-publish`
    signing
    java
    com.google.cloud.artifactregistry.`gradle-plugin`
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(tasks.processResources)
    archiveClassifier = "sources"
    destinationDirectory = layout.buildDirectory
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.schlaubi"
            artifactId = "mikbot-${project.name}"
            afterEvaluate {
                version = project.version as String
            }

            from(components["java"])
            artifact(sourcesJar)


            pom {
                name = "mikbot"
                description = "A modular framework for building Discord bots"
                url = "https://github.com/DRSchlaubi/mikmusic"

                organization {
                    name = "Schlaubi"
                    url = "https://github.com/DRSchlaubi"
                }

                developers {
                    developer {
                        name = "Michael Rittmeister"
                    }
                }

                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/mikbot/games/issues"
                }

                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/mikbot/games.git"
                    developerConnection = "scm:git:ssh://git@github.com:mikbot/games.git"
                    url = "https://github.com/mikbot/games.git"
                }
            }

            repositories {
                maven("artifactregistry://europe-west3-maven.pkg.dev/mik-music/mikbot") {
                    credentials {
                        username = "_json_key_base64"
                        password = System.getenv("GOOGLE_KEY")?.toByteArray()?.let {
                            Base64.getEncoder().encodeToString(it)
                        }
                    }

                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")?.toString()
    val signingPassword = System.getenv("SIGNING_KEY_PASSWORD")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
        sign(publishing.publications["maven"])
    }
}

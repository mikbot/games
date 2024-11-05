package dev.schlaubi.mikbot.game.api.module.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalUser
import dev.kord.rest.builder.message.embed
import dev.schlaubi.mikbot.game.api.GameStats
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.games.translations.GameApiTranslations
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.translate
import org.litote.kmongo.div
import org.litote.kmongo.gt

class UnoProfileArguments : Arguments() {
    val target by optionalUser {
        name = GameApiTranslations.Commands.Profile.Arguments.User.name
        description = GameApiTranslations.Commands.Profile.Arguments.User.description
    }
}

/**
 * Adds a /profile command to this [profileCommand].
 */
fun GameModule<*, *>.profileCommand() = publicSubCommand(::UnoProfileArguments) {
    name = GameApiTranslations.Commands.Profile.name
    description = GameApiTranslations.Commands.Profile.description

    action {
        val target = arguments.target ?: user

        val stats = gameStats.findOneById(target.id)?.stats

        if (stats == null) {
            respond {
                content = translate(GameApiTranslations.Commands.Profile.Profile.empty)
            }
            return@action
        }

        respond {
            val author = this@profileCommand.kord.getUser(user.id)
            embed {
                author {
                    name = author?.username ?: "<crazy person>"
                    icon = author?.effectiveAvatar
                }

                field {
                    name = translate(GameApiTranslations.Commands.Profile.wins)
                    value = stats.wins.toString()
                    inline = true
                }

                field {
                    name = translate(GameApiTranslations.Commands.Profile.losses)
                    value = stats.losses.toString()
                    inline = true
                }

                field {
                    name = translate(GameApiTranslations.Commands.Profile.ratio)
                    value = stats.ratio.formatPercentage()
                    inline = true
                }

                field {
                    name = translate(GameApiTranslations.Commands.Profile.played)
                    value = (stats.wins + stats.losses).toString()
                    inline = true
                }

                field {
                    name = translate(GameApiTranslations.Commands.Profile.rank)
                    val otherPlayerCount = gameStats.countDocuments(UserGameStats::stats / GameStats::ratio gt 0.0)
                    value = (otherPlayerCount + 1).toString()
                    inline = true
                }
            }
        }
    }
}

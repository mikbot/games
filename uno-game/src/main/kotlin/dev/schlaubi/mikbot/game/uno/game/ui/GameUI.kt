package dev.schlaubi.mikbot.game.uno.game.ui

import dev.kord.common.Color
import dev.kord.common.kColor
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.i18n.toKey
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.uno.Direction
import dev.schlaubi.uno.UnoColor
import java.util.*
import java.awt.Color as JColor

suspend fun EmbedBuilder.startUI(uno: DiscordUnoGame) {
    field {
        name = uno.translate(UnoTranslations.Uno.Game.extremeMode)
        value = uno.extremeMode.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.flashMode)
        value = uno.flashMode.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.dropIns)
        value = uno.allowDropIns.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.drawUntilPlayable)
        value = uno.drawUntilPlayable.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.forcePlay)
        value = uno.forcePlay.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.drawCardStacking)
        value = uno.allowDrawCardStacking.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.bluffing)
        value = uno.allowBluffing.toString()
    }

    field {
        name = uno.translate(UnoTranslations.Uno.Game.`07`)
        value = uno.useSpecial7and0.toString()
    }
}

suspend fun EmbedBuilder.welcomeMessage(uno: DiscordUnoGame) {
    with(uno) {
        if (players.isNotEmpty()) {
            field {
                val playingPlayers = game.players
                val actualPlayers =
                    if (!flashMode && game.direction == Direction.COUNTER_CLOCKWISE) {
                        playingPlayers.reversed()
                    } else {
                        playingPlayers
                    }
                name = translate(UnoTranslations.Uno.Game.players)
                value = actualPlayers.joinToString(", ") {
                    val mention = it.user.mention
                    if (running) {
                        "$mention (${it.deck.size})"
                    } else {
                        mention
                    }
                }
            }
        }

        if (wonPlayers.isNotEmpty()) {
            field {
                name = translate(UnoTranslations.Uno.Game.wonPlayers)
                value = wonPlayers.joinToString(", ") { it.user.mention }
            }
        }

        color = game.topCard.color.kColor
        thumbnail {
            url = game.topCard.imageUrl
        }

        field {
            name = translate(UnoTranslations.Uno.Game.lastPlayer)

            value = lastPlayer?.user?.mention ?: translate(UnoTranslations.Uno.Game.Player.none)
            inline = true
        }

        field {
            name = translate(UnoTranslations.Uno.Game.currentPlayer)
            value = currentPlayer?.user?.mention.toString()
            inline = true
        }

        field {
            name = translate(UnoTranslations.Uno.Game.nextPlayer)
            value = nextPlayer.user.mention
            inline = true
        }

        if (game.drawCardSum >= 1) {
            field {
                name = translate(UnoTranslations.Uno.Game.drawCardSum)
                value = game.drawCardSum.toString()
                inline = false
            }
        }

        field {
            name = translate(UnoTranslations.Uno.Game.cardsPlayed)
            value = game.cardsPlayed.toString()
        }

        field {
            name = translate(UnoTranslations.Uno.Game.topCard)
            value = translate(game.topCard.translationKey)
            inline = true
        }

        field {
            name = translate(UnoTranslations.Uno.Game.direction)
            value = translate(("uno.direction." + game.direction.name.lowercase(Locale.ENGLISH)).toKey())
            inline = true
        }
    }
}

private val UnoColor.kColor: Color
    get() {
        val jColor = when (this) {
            UnoColor.RED -> JColor.RED
            UnoColor.YELLOW -> JColor.YELLOW
            UnoColor.BLUE -> JColor.BLUE
            UnoColor.GREEN -> JColor.GREEN
        }

        return jColor.kColor
    }

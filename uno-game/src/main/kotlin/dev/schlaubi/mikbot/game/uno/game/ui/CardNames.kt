package dev.schlaubi.mikbot.game.uno.game.ui

import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.uno.cards.*
import java.util.*

private const val BASE_TRANSLATION = "uno.cards."

val Card.translationKey: Key
    get() = when (this) {
        is ColoredCard -> this.translationKey.toKey()
        is AbstractWildCard -> this.translationKey.toKey()
        else -> error("Could not find image for card: $this")
    }

val AbstractWildCard.translationKey: String
    get() = when (this) {
        is PlayedCard -> {
            val name = if (this is DrawingCard) {
                "wild.draw4"
            } else {
                "wild"
            }

            "$coloredTranslation$name"
        }
        is WildCard -> "${BASE_TRANSLATION}wild.wild"
        is DrawingCard -> "${BASE_TRANSLATION}wild.draw4"
        else -> error("Could not find image for card: $this")
    }

val ColoredCard.translationKey: String
    get() {
        val name = when (this) {
            is NumberedCard -> number.toString()
            is DrawTwoCard -> "draw2"
            is ReverseCard -> "reverse"
            is SkipCard -> "skip"
            is DiscardAllCardsCard -> "discard_all_cards"
            is SlapCard -> "slap"
        }

        return "$coloredTranslation$name"
    }

private val PlayedCard.coloredTranslation: String
    get() = BASE_TRANSLATION + color.name.lowercase(Locale.ENGLISH) + "."

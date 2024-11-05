package dev.schlaubi.mikbot.game.hangman.game

import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.games.translations.HangmanTranslations
import dev.schlaubi.mikbot.plugin.api.util.embed
import java.util.*

suspend fun GameState.Guessing.toEmbed(game: HangmanGame) = embed {
    description = buildString {

        append(game.translate(HangmanTranslations.Game.Ui.word))
        append("```")
        word.forEach {
            if (it.uppercaseChar() in chars || it.isWhitespace()) {
                append(it)
            } else {
                append('_')
            }
        }
        append("```")

        if (wrongChars.isNotEmpty() || blackList.isNotEmpty()) {
            repeat(2) {
                appendLine()
            }

            append(HangmanGame.googologo.take(wrongChars.size + blackList.size).joinToString(" ") { it.mention })
        }
    }

    if (wrongChars.isNotEmpty()) {
        field {
            name = game.translate(HangmanTranslations.Game.Ui.wrongCharacters)
            value = wrongChars.joinToString("`, `", "`", "`") { it.uppercase(Locale.ENGLISH) }
        }
    }

    if (blackList.isNotEmpty()) {
        field {
            name = game.translate(HangmanTranslations.Game.Ui.wrongWords)
            value = blackList.joinToString("`, `", "`", "`")
        }
    }
}

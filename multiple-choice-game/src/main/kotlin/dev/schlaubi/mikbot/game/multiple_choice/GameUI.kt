package dev.schlaubi.mikbot.game.multiple_choice

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.api.module.commands.formatPercentage
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.multiple_choice.player.Statistics
import dev.schlaubi.mikbot.games.translations.MultipleChoiceTranslations
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import java.util.*

/**
 * Context of a submitted answer.
 *
 * @property answerIndex the index of the answer the user selected (or null if the user did not answer)
 * @property correct whether the answer ist correct
 * @property response the [EphemeralMessageInteractionResponseBehavior] of the button click
 * @property points how many points have been awarded
 */
data class AnswerContext(
    val answerIndex: Int?,
    val correct: Boolean,
    val interactionCreateEvent: InteractionCreateEvent,
    val response: EphemeralMessageInteractionResponseBehavior,
    val points: Int,
    val game: MultipleChoiceGame<*, *, *>,
) {
    val emoji: DiscordEmoji
        get() = when (answerIndex) {
            null -> Emojis.clock
            0 -> Emojis.one
            1 -> Emojis.two
            2 -> Emojis.three
            3 -> Emojis.four
            else -> error("Unexpected AnswerIndex $")
        }
}

internal suspend fun EmbedBuilder.addPlayers(
    players: Map<UserBehavior, AnswerContext>,
    showCorrect: Boolean = true,
    game: MultipleChoiceGame<*, *, *>,
) {
    field {
        name = game.translate(MultipleChoiceTranslations.Ui.answers)
        value = if (players.isNotEmpty()) {
            players.map { (player, answer) ->
                val checkEmoji: Any = when {
                    !showCorrect -> ""
                    answer.correct -> "${Emojis.whiteCheckMark} (+${answer.points})"
                    else -> Emojis.noEntrySign
                }

                "${player.mention} - ${answer.emoji} $checkEmoji"
            }.joinToString("\n")
        } else {
            "No one answered :("
        }
    }
}

internal suspend fun EmbedBuilder.addUserStats(
    userBehavior: UserBehavior,
    stats: Statistics,
    points: Int,
    locale: Locale,
    game: MultipleChoiceGame<*, *, *>,
) {
    author {
        val user = userBehavior.asUser()
        name = user.username
        icon = user.effectiveAvatar
    }

    field {
        name = game.translate(MultipleChoiceTranslations.Stats.CorrectAnswers.title, locale = locale)
        val percentage = (stats.points.toDouble() / stats.gameSize.toDouble()).formatPercentage()
        value =
            game.translate(MultipleChoiceTranslations.Stats.CorrectAnswers.value, stats.points, stats.gameSize, percentage, locale = locale)
    }

    field {
        name = game.translate(MultipleChoiceTranslations.Stats.TotalPoints.title, locale = locale)
        value = game.translate(MultipleChoiceTranslations.Stats.value, points, locale = locale)
    }

    field {
        name = game.translate(MultipleChoiceTranslations.Stats.AverageResponseTime.title, locale = locale)
        value = game.translate(MultipleChoiceTranslations.Stats.value, stats.average, locale = locale)
    }

}

internal suspend fun <P : MultipleChoicePlayer> EmbedBuilder.addGameEndEmbed(game: MultipleChoiceGame<P, *, *>) {
    val user = game.wonPlayers.firstOrNull()?.user ?: return
    val player = with(game) { user.gamePlayer }
    addUserStats(
        user,
        game.gameStats[user.id] ?: Statistics(0, emptyList(), game.quizSize),
        game.mechanics.pointsDistributor.retrievePointsForPlayer(player),
        game.locale(),
        game
    )
}

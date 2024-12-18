package dev.schlaubi.mikbot.game.trivia.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.i18n.TranslationsProvider
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.Rematchable
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.multiple_choice.MultipleChoiceGame
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.DefaultStreakGameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.trivia.QuestionContainer
import dev.schlaubi.mikbot.game.trivia.TriviaModule
import dev.schlaubi.mikbot.game.trivia.open_trivia.Question
import dev.schlaubi.mikbot.games.translations.TriviaTranslations
import io.ktor.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REQUEST_RAW = "request_raw"

class TriviaGame(
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
    host: UserBehavior,
    module: TriviaModule,
    quizSize: Int,
    questionContainer: QuestionContainer,
) : MultipleChoiceGame<MultipleChoicePlayer, TriviaQuestion, QuestionContainer>(
    host,
    module.asType,
    quizSize,
    questionContainer,
    DefaultStreakGameMechanics()
), Rematchable<MultipleChoicePlayer, TriviaGame>,
    AutoJoinableGame<MultipleChoicePlayer> {
    override val answerDelay: Duration = 1.seconds
    override val rematchThreadName: String = "trivia-rematch"

    override suspend fun EmbedBuilder.addWelcomeMessage() {
        if (questionContainer.category != null) {
            field {
                name = translate(TriviaTranslations.Trivia.Question.category)
                value = translate(questionContainer.category!!.readableName)
                inline = true
            }
        }

        if (questionContainer.difficulty != null) {
            field {
                name = translate(TriviaTranslations.Trivia.Question.difficulty)
                value = translate(questionContainer.difficulty!!.readableName)
                inline = true
            }
        }

        if (questionContainer.type != null) {
            field {
                name = translate(TriviaTranslations.Trivia.Question.type)
                value = translate(questionContainer.type!!.readableName)
                inline = true
            }
        }

        footer {
            text = "Powered by Open Trivia & Google"
        }
    }

    override fun obtainNewPlayer(user: User): MultipleChoicePlayer = MultipleChoicePlayer(user)

    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: dev.kord.common.Locale?,
    ): MultipleChoicePlayer = MultipleChoicePlayer(user)

    override suspend fun EmbedBuilder.addQuestion(question: TriviaQuestion, hideCorrectAnswer: Boolean) {
        addQuestion(question.parent, hideCorrectAnswer)
    }

    private suspend fun EmbedBuilder.addQuestion(question: Question, hideCorrectAnswer: Boolean) {
        title = question.title

        field {
            name = translate(TriviaTranslations.Trivia.Question.category)
            value = translate(question.category.readableName)
            inline = true
        }

        field {
            name = translate(TriviaTranslations.Trivia.Question.difficulty)
            value = translate(question.difficulty.readableName)
            inline = true
        }

        if (!hideCorrectAnswer) {
            field {
                name = translate(TriviaTranslations.Trivia.Question.correctAnswer)
                value = question.correctAnswer
                inline = false
            }
        }
    }

    override suspend fun MessageCreateBuilder.questionUI(question: TriviaQuestion) {
        if (question.original != null) {
            actionRow {
                interactionButton(ButtonStyle.Primary, REQUEST_RAW) {
                    label = translate(TriviaTranslations.Trivia.Game.requestRaw)
                }
            }
        }
    }

    override suspend fun ComponentInteractionCreateEvent.handle(question: TriviaQuestion): Boolean {
        if (question.original != null && interaction.componentId == REQUEST_RAW) {
            interaction.respondEphemeral {
                embed {
                    addQuestion(question.original, true)
                }

                actionRow {
                    question.original.allAnswers.forEach {
                        interactionButton(ButtonStyle.Secondary, generateNonce()) {
                            label = it
                            disabled = true
                        }
                    }
                }
            }

            return true
        }

        return false
    }

    override suspend fun rematch(thread: ThreadChannelBehavior, welcomeMessage: Message): TriviaGame {
        val newQuestionContainer = QuestionContainer(
            questionContainer.size,
            questionContainer.difficulty,
            questionContainer.category,
            questionContainer.type,
            locale(),
            translationsProvider,
            module as TriviaModule
        )

        return TriviaGame(
            thread,
            welcomeMessage,
            translationsProvider,
            host,
            module as TriviaModule,
            quizSize,
            newQuestionContainer
        ).apply {
            players.addAll(this@TriviaGame.players)
        }
    }
}

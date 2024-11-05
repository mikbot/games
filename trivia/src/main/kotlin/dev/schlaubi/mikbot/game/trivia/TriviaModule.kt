package dev.schlaubi.mikbot.game.trivia

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.kord.common.asJavaLocale
import dev.kordex.core.i18n.EMPTY_KEY
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.trivia.game.TriviaGame
import dev.schlaubi.mikbot.game.trivia.open_trivia.Category
import dev.schlaubi.mikbot.game.trivia.open_trivia.Difficulty
import dev.schlaubi.mikbot.game.trivia.open_trivia.Type
import dev.schlaubi.mikbot.games.translations.TriviaTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

class StartTriviaArguments : Arguments() {
    val amount by defaultingInt {
        name = TriviaTranslations.Commands.Start.Arguments.Amount.name
        description = TriviaTranslations.Commands.Start.Arguments.Amount.description

        defaultValue = 10
    }

    val category by optionalEnumChoice<Category> {
        name = TriviaTranslations.Commands.Start.Arguments.Category.name
        description = TriviaTranslations.Commands.Start.Arguments.Category.description
        typeName = EMPTY_KEY
    }

    val difficulty by optionalEnumChoice<Difficulty> {
        name = TriviaTranslations.Commands.Start.Arguments.Difficulty.name
        description = TriviaTranslations.Commands.Start.Arguments.Difficulty.description
        typeName = EMPTY_KEY
    }

    val type by optionalEnumChoice<Type> {
        name = TriviaTranslations.Commands.Start.Arguments.Type.name
        description = TriviaTranslations.Commands.Start.Arguments.Type.description
        typeName = EMPTY_KEY
    }
}

class TriviaModule(context: PluginContext) : GameModule<MultipleChoicePlayer, TriviaGame>(context) {
    override val name: String = "trivia"
    override val gameStats: CoroutineCollection<UserGameStats> = TriviaDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand(
            TriviaTranslations.Trivia.Game.title, "trivia", ::StartTriviaArguments, {

                val locale = event.interaction.guildLocale?.convertToISO()?.asJavaLocale()
                    ?: bot.settings.i18nBuilder.defaultLocale

                val questions = try {
                    QuestionContainer(
                        arguments.amount,
                        arguments.difficulty,
                        arguments.category,
                        arguments.type,
                        locale,
                        get(),
                        this@TriviaModule
                    )
                } catch (e: IllegalArgumentException) {
                    discordError(TriviaTranslations.Commands.Trivia.Start.noQuestions)
                }

                questions to locale
            },
            { (questionContainer, _), message, thread ->
                TriviaGame(
                    thread,
                    message,
                    get(),
                    user,
                    this@TriviaModule,
                    arguments.amount,
                    questionContainer
                )
            }
        )

        stopGameCommand()
        profileCommand()
        leaderboardCommand(TriviaTranslations.Trivia.Stats.title)
    }
}

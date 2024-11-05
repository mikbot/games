package dev.schlaubi.mikbot.game.trivia.open_trivia

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.games.translations.TriviaTranslations
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import dev.schlaubi.mikbot.game.multiple_choice.Question as GameQuestion

@Serializable
data class OpenTriviaResponse(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("response_message") val responseMessage: String? = null,
    val results: List<Question> = emptyList(),
    val token: String? = null
)

@Serializable
data class Question(
    val category: Category,
    val type: Type,
    val difficulty: Difficulty,
    @SerialName("question") override val title: String,
    @SerialName("correct_answer") override val correctAnswer: String,
    @SerialName("incorrect_answers") override val incorrectAnswers: List<String>,
    private val sortedAnswers: List<String>? = null
) : GameQuestion {
    override val allAnswers: List<String>
        get() = sortedAnswers
            ?: if (type == Type.MULTIPLE_CHOICE) super.allAnswers.shuffled() else super.allAnswers.sortedByDescending {
                it.equals(
                    "true", ignoreCase = true
                )
            }
}

@Serializable
enum class Type(override val readableName: Key) : ChoiceEnum {

    @SerialName("boolean")
    TRUE_FALSE(TriviaTranslations.Trivia.Question.Type.trueFalse),

    @SerialName("multiple")
    MULTIPLE_CHOICE(TriviaTranslations.Trivia.Question.Type.multipleChoice);

}

@Serializable
enum class Difficulty(override val readableName: Key) : ChoiceEnum {
    @SerialName("easy")
    EASY(TriviaTranslations.Trivia.Question.Difficulty.easy),

    @SerialName("medium")
    MEDIUM(TriviaTranslations.Trivia.Question.Difficulty.medium),

    @SerialName("hard")
    HARD(TriviaTranslations.Trivia.Question.Difficulty.hard);

}

@Serializable(with = Category.Serializer::class)
enum class Category(val id: Int, val apiName: String, override val readableName: Key) : ChoiceEnum {
    GENERAL_KNOWLEDGE(
        9,
        "General Knowledge",
        TriviaTranslations.Trivia.Question.Category.generalKnowledge
    ),
    ENTERTAINMENT_BOOKS(
        10,
        "Entertainment: Books",
        TriviaTranslations.Trivia.Question.Category.entertainmentBooks
    ),
    ENTERTAINMENT_FILM(
        11,
        "Entertainment: Film",
        TriviaTranslations.Trivia.Question.Category.entertainmentFilm
    ),
    ENTERTAINMENT_MUSIC(
        12,
        "Entertainment: Music",
        TriviaTranslations.Trivia.Question.Category.entertainmentMusic
    ),
    ENTERTAINMENT_MUSICALS_THEATRES(
        13,
        "Entertainment: Musicals & Theatres",
        TriviaTranslations.Trivia.Question.Category.entertainmentMusicalsTheatres
    ),
    ENTERTAINMENT_TELEVISION(
        14,
        "Entertainment: Television",
        TriviaTranslations.Trivia.Question.Category.entertainmentTelevision
    ),
    ENTERTAINMENT_VIDEO_GAMES(
        15,
        "Entertainment: Video Games",
        TriviaTranslations.Trivia.Question.Category.entertainmentVideoGames
    ),
    ENTERTAINMENT_BOARD_GAMES(
        16,
        "Entertainment: Board Games",
        TriviaTranslations.Trivia.Question.Category.entertainmentBoardGames
    ),
    SCIENCE_NATURE(
        17,
        "Science & Nature",
        TriviaTranslations.Trivia.Question.Category.scienceNature
    ),
    SCIENCE_COMPUTERS(
        18,
        "Science: Computers",
        TriviaTranslations.Trivia.Question.Category.scienceComputers
    ),
    SCIENCE_MATHEMATICS(
        19,
        "Science: Mathematics",
        TriviaTranslations.Trivia.Question.Category.scienceMathematics
    ),
    MYTHOLOGY(20, "Mythology", TriviaTranslations.Trivia.Question.Category.mythology),
    SPORTS(21, "Sports", TriviaTranslations.Trivia.Question.Category.sports),
    GEOGRAPHY(
        22,
        "Geography",
        TriviaTranslations.Trivia.Question.Category.geography
    ),
    HISTORY(23, "History", TriviaTranslations.Trivia.Question.Category.history),
    POLITICS(24, "Politics", TriviaTranslations.Trivia.Question.Category.politics), ART(
        25,
        "Art",
        TriviaTranslations.Trivia.Question.Category.art
    ),
    CELEBRITIES(
        26,
        "Celebrities",
        TriviaTranslations.Trivia.Question.Category.celebrities
    ),
    ANIMALS(27, "Animals", TriviaTranslations.Trivia.Question.Category.animals), VEHICLES(28, "Vehicles", TriviaTranslations.Trivia.Question.Category.vehicles), ENTERTAINMENT_COMICS(
        29,
        "Entertainment: Comics",
        TriviaTranslations.Trivia.Question.Category.entertainmentComics
    );

    companion object Serializer : KSerializer<Category> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Category) {
            encoder.encodeString(value.apiName)
        }

        override fun deserialize(decoder: Decoder): Category {
            val apiName = decoder.decodeString()
            return entries.firstOrNull { it.apiName == apiName } ?: GENERAL_KNOWLEDGE
        }
    }
}

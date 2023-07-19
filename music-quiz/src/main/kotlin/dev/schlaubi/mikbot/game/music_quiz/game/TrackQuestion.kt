package dev.schlaubi.mikbot.game.music_quiz.game

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikbot.game.multiple_choice.Question

class TrackQuestion(
    override val title: String,
    override val correctAnswer: String,
    override val incorrectAnswers: List<String>,
    val track: Track
) : Question

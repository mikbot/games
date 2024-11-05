package dev.schlaubi.mikbot.game.music_quiz

import dev.kordex.core.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object MusicQuizDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("song_quiz_stats")
    val likedSongs = database.getCollection<LikedSongs>("liked_songs")
}

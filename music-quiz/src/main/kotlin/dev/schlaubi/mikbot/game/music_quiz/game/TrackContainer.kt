package dev.schlaubi.mikbot.game.music_quiz.game

import dev.arbjerg.lavalink.protocol.v4.Playlist
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikbot.game.multiple_choice.QuestionContainer
import dev.schlaubi.stdx.core.poll
import java.util.*
import kotlin.random.Random

class TrackContainer @PublishedApi internal constructor(
    val spotifyPlaylist: Playlist,
    private val tracks: List<Track>,
    private val artistPool: LinkedList<String>,
    private val songNamePool: LinkedList<String>
) : QuestionContainer<TrackQuestion> {

    private val allArtists = ArrayList(artistPool)
    private val allSongs = ArrayList(songNamePool)

    val artistCount: Int
        get() = allArtists.size

    override fun iterator(): Iterator<TrackQuestion> = TrackQuestionIterator()

    fun pollArtistNames(blacklist: String, amount: Int = 3): List<String?> =
        artistPool.poll(allArtists, blacklist, amount)

    fun pollSongNames(blacklist: String, amount: Int = 3): List<String> =
        songNamePool.poll(allSongs, blacklist, amount).filterNotNull()

    private fun LinkedList<String>.poll(backupPool: List<String>, blacklist: String, amount: Int): List<String?> {
        if (size <= amount) {
            addAll(backupPool)
        }
        val allowed = LinkedList(this - blacklist)
        val options = allowed.poll(amount)
        removeAll((options + blacklist).toSet())

        return options
    }

    private inner class TrackQuestionIterator : Iterator<TrackQuestion> {
        val backupAnswers get() = generateSequence {
            "Yeah I don't have an answer here but here is a random Number: ${Random.nextInt(1000)}"
        }

        private val parent = tracks.iterator()

        override fun hasNext(): Boolean = parent.hasNext()

        override fun next(): TrackQuestion {
            val track = parent.next()

            val (wrongAnswers, correctAnswer, title) = decideTurnParameters(track)

            val allWrongAnswers = wrongAnswers + backupAnswers.take(3 - wrongAnswers.size)

            return TrackQuestion(
                title, correctAnswer, allWrongAnswers, track
            )
        }
    }

    companion object {
        operator fun invoke(
            playlist: Playlist,
            size: Int
        ): TrackContainer {
            val playlistTracks = playlist.tracks
                .toList()
                .shuffled()
            val artists = HashSet<String>(playlistTracks.size)
            val names = HashSet<String>(playlistTracks.size)
            playlistTracks.forEach {
                artists.add(it.info.author)
                names.add(it.info.title)
            }
            val artistPool = LinkedList(artists)
            val songNamePool = LinkedList(names)

            val tracks = playlistTracks.take(size.coerceAtMost(playlistTracks.size))

            return TrackContainer(playlist, tracks, artistPool, songNamePool)
        }
    }
}

private fun TrackContainer.decideTurnParameters(track: Track): GuessContext {
    val mode = if(artistCount < 4) {
        if (Random.nextInt(1, 100) == 50) GuessingMode.ARTIST else GuessingMode.NAME
    } else {
        GuessingMode.entries.random()
    }
    return when (mode) {
        GuessingMode.NAME -> GuessContext(
            pollSongNames(track.info.title),
            track.info.title,
            "Guess the Name of this song",
        )
        GuessingMode.ARTIST -> {
            val artistName = track.info.author
            val fillerArtists = pollArtistNames(artistName)

            if (artistCount < 4) {
                val nonNullNames = fillerArtists.filterNotNull()
                val correctArtistPool = generateSequence { artistName }
                    .mapIndexed { index, artist -> "$artist #${index + 1}" }
                    .take(4 - nonNullNames.size) // repeat correct option to fill for missing once and choose the correct one at random
                    .toList()
                val correct = correctArtistPool.random()

                GuessContext(
                    (nonNullNames + correctArtistPool) - correct,
                    correct,
                    "Looks like you wanted to cheat, by using a playlist with less than 4 artists in it, so have fun guessing",
                )
            } else {
                GuessContext(
                    fillerArtists.filterNotNull(),
                    artistName,
                    "Guess the Artist of this song",
                )
            }
        }
    }
}

private data class GuessContext(
    val wrongOptionSource: List<String>,
    val correctOption: String,
    val title: String,
)

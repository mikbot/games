package dev.schlaubi.mikbot.game.music_quiz

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedSongs(
    @SerialName("_id")
    val owner: Snowflake,
    val songs: Set<LikedSong>
)

@Serializable
data class LikedSong(
    val name: String,
    val artist: String,
    val url: String
)

fun Track.toLikedSong() = LikedSong(info.title, info.author, info.uri!!)

package dev.schlaubi.mikbot.game.music_quiz.game

import dev.kordex.core.koin.KordExContext
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.games.translations.SongQuizTranslations

private val imageColorClient by KordExContext.get().inject<ImageColorClient>()

suspend fun EmbedBuilder.addTrack(track: Track, game: SongQuizGame) {
    val extendedInfo = runCatching { track.lavaSrcInfo }.getOrNull()
    author {
        name = track.info.title
        runCatching {
            icon = extendedInfo?.artistArtworkUrl
        }

        url = track.info.uri
    }

    track.info.artworkUrl?.let { thumbnailUrl ->
        thumbnail {
            url = thumbnailUrl
        }
        imageColorClient.fetchImageColorOrNull(thumbnailUrl)?.let { imageColor ->
            color = Color(imageColor)
        }
    }

    if (extendedInfo?.albumName != null) {
        field {
            name = game.translate(SongQuizTranslations.Game.Ui.album)
            value = extendedInfo.albumName!!
        }
    }

    field {
        name = game.translate(SongQuizTranslations.Game.Ui.artists)
        value = track.info.author
    }

    footer {
        text = game.translate(SongQuizTranslations.Game.Ui.footer)
    }
}

package dev.schlaubi.mikbot.game.music_quiz

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Playlist
import dev.kord.core.behavior.GuildBehavior
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.music_quiz.game.SongQuizGame
import dev.schlaubi.mikbot.game.music_quiz.game.TrackContainer
import dev.schlaubi.mikbot.games.translations.SongQuizTranslations
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.util.musicModule
import org.koin.core.component.get
import org.litote.kmongo.coroutine.CoroutineCollection

open class SongQuizSizeArguments : Arguments() {
    val size by defaultingInt {
        name = SongQuizTranslations.Commands.Start.Arguments.Size.name
        description = SongQuizTranslations.Commands.Start.Arguments.Size.description
        defaultValue = 25
    }
}

open class SongQuizPlaylistArguments : SongQuizSizeArguments() {
    val playlist by string {
        name = SongQuizTranslations.Commands.Start.Arguments.Playlist.name
        description = SongQuizTranslations.Commands.Start.Arguments.Playlist.description
    }

    init {
        // Fix optional argument size being before required argument playlist
        args.reverse()
    }
}

class SongQuizModule(context: PluginContext) : GameModule<MultipleChoicePlayer, SongQuizGame>(context) {
    override val name: String = "song-quiz"
    override val gameStats: CoroutineCollection<UserGameStats> = MusicQuizDatabase.stats
    private val musicModule: MusicModule by extension()

    override suspend fun gameSetup() {
        spotifyPlaylistSongs()
        startGameCommand(
            SongQuizTranslations.Commands.Start.Playlist.name,
            SongQuizTranslations.Commands.Start.Playlist.description,
            ::SongQuizPlaylistArguments,
            SongQuizPlaylistArguments::playlist
        )
        stopGameCommand()
        leaderboardCommand(SongQuizTranslations.Commands.SongQuiz.Leaderboard.Page.title)
        profileCommand()
        likedSongsCommand()
    }

    suspend fun startGameCommand(
        name: String,
        description: String,
        playlistUrl: String
    ) = startGameCommand(name.toKey(), description.toKey(), ::SongQuizSizeArguments) { playlistUrl }

    private fun <A : SongQuizSizeArguments> startGameCommand(
        name: Key,
        description: Key,
        arguments: () -> A,
        playlistArgument: A.() -> String
    ) = this@SongQuizModule.startGameCommand(
        SongQuizTranslations.SongQuiz.Game.title,
         "song-quiz",
        arguments,
        prepareData@{
            val playlist = getPlaylist(safeGuild, this.arguments.playlistArgument())
            if (playlist == null) {
                respond {
                    content = translate(SongQuizTranslations.Commands.SongQuiz.StartGame.notFound)
                }
                return@prepareData null
            }

            TrackContainer(playlist, this.arguments.size)
        },
        { trackContainer, message, thread ->
            SongQuizGame(
                user,
                this@SongQuizModule,
                this.arguments.size.coerceAtMost(trackContainer.spotifyPlaylist.tracks.size),
                musicModule.getMusicPlayer(safeGuild),
                trackContainer,
                thread,
                message,
                get()
            )
        },
        { joinSameChannelCheck(bot) },
        name,
        description
    )
}

private suspend fun Extension.getPlaylist(guild: GuildBehavior, url: String): Playlist? {
    val node = musicModule.getMusicPlayer(guild).node
    return (node.loadItem(url) as? LoadResult.PlaylistLoaded)?.data
}

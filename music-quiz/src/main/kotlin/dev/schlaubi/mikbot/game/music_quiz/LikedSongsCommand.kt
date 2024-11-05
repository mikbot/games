package dev.schlaubi.mikbot.game.music_quiz

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.int
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.games.translations.SongQuizTranslations
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule

class RemoveArguments : Arguments() {
    val position by int {
        name = SongQuizTranslations.Commands.SongLikes.Remove.Arguments.Position.name
        description = SongQuizTranslations.Commands.SongLikes.Remove.Arguments.Position.description
    }
}

suspend fun SongQuizModule.likedSongsCommand() = ephemeralSlashCommand {
    name = SongQuizTranslations.Commands.SongLikes.name
    description = SongQuizTranslations.Commands.SongLikes.description

    ephemeralSubCommand {
        name = SongQuizTranslations.Commands.SongLikes.Show.name
        description = SongQuizTranslations.Commands.SongLikes.Show.description

        check {
            musicQuizAntiCheat(musicModule)
        }

        action {
            val songs = MusicQuizDatabase.likedSongs.findOneById(user.id)
            if (songs?.songs.isNullOrEmpty()) {
                respond {
                    content = translate(SongQuizTranslations.Commands.SongLikes.empty)
                }
                return@action
            }

            editingPaginator {
                forList(
                    user,
                    songs!!.songs.toList(),
                    { "[${it.name} - ${it.artist}](${it.url})" },
                    { current, all -> translate(SongQuizTranslations.Commands.SongLikes.Show.title, current, all) }
                )
            }.send()
        }
    }

    ephemeralSubCommand(::RemoveArguments) {
        name = SongQuizTranslations.Commands.SongLikes.Remove.name
        description = SongQuizTranslations.Commands.SongLikes.Remove.description

        check {
            musicQuizAntiCheat(musicModule)
        }

        action {
            val songs = MusicQuizDatabase.likedSongs.findOneById(user.id)
            if (songs == null) {
                respond {
                    content = translate(SongQuizTranslations.Commands.SongLikes.empty)
                }
                return@action
            }

            val newSongList = songs.songs.toMutableList()
            if (arguments.position > songs.songs.size) {
                respond {
                    content = translate(SongQuizTranslations.Commands.SongLikes.Remove.outOfBounds)
                }
                return@action
            }
            val removedSong = newSongList.removeAt(arguments.position - 1)

            val newSongs = songs.copy(songs = newSongList.toSet())
            MusicQuizDatabase.likedSongs.save(newSongs)

            respond {
                content = translate(SongQuizTranslations.Commands.SongLikes.removed, removedSong.name)
            }
        }
    }
}

package dev.schlaubi.mikbot.game.music_quiz.game

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.x.emoji.Emojis
import dev.kordex.core.i18n.TranslationsProvider
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikbot.game.api.AutoJoinableGame
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.multiple_choice.MultipleChoiceGame
import dev.schlaubi.mikbot.game.multiple_choice.mechanics.DefaultStreakGameMechanics
import dev.schlaubi.mikbot.game.multiple_choice.player.MultipleChoicePlayer
import dev.schlaubi.mikbot.game.music_quiz.LikedSongs
import dev.schlaubi.mikbot.game.music_quiz.MusicQuizDatabase
import dev.schlaubi.mikbot.game.music_quiz.SongQuizModule
import dev.schlaubi.mikbot.game.music_quiz.toLikedSong
import dev.schlaubi.mikbot.games.translations.SongQuizTranslations
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.PersistentPlayerState
import dev.schlaubi.mikmusic.player.applyToPlayer
import kotlinx.coroutines.launch

class SongQuizGame(
    host: UserBehavior,
    module: SongQuizModule,
    quizSize: Int,
    private val musicPlayer: MusicPlayer,
    trackContainer: TrackContainer,
    override val thread: ThreadChannelBehavior,
    override val welcomeMessage: Message,
    override val translationsProvider: TranslationsProvider,
) : MultipleChoiceGame<MultipleChoicePlayer, TrackQuestion, TrackContainer>(
    host,
    module.asType,
    quizSize,
    trackContainer,
    DefaultStreakGameMechanics()
), AutoJoinableGame<MultipleChoicePlayer> {
    override val playerRange: IntRange = 1..10
    private var started = false
    private var beforePlayerState: PersistentPlayerState? = null

    override suspend fun EmbedBuilder.addWelcomeMessage() {
        runCatching {
            field {
                name = translate(SongQuizTranslations.Game.Ui.playlist)
                value = questionContainer.spotifyPlaylist.lavaSrcInfo.url!!
            }
        }
    }

    override fun obtainNewPlayer(user: User): MultipleChoicePlayer = SongQuizPlayer(user)
    override suspend fun obtainNewPlayer(
        user: User,
        ack: EphemeralMessageInteractionResponseBehavior,
        loading: FollowupMessage,
        userLocale: Locale?,
    ): SongQuizPlayer =
        SongQuizPlayer(user).also {
            loading.edit { content = translate(it, SongQuizTranslations.SongQuiz.Controls.joined) }
        }

    override suspend fun askQuestion(question: TrackQuestion) {
        val preview = runCatching { question.track.lavaSrcInfo.previewUrl }.getOrNull()
        if (preview != null) {
            musicPlayer.player.searchAndPlayTrack(preview) {}
        } else {
            musicPlayer.player.playTrack(question.track)
        }

        super.askQuestion(question)
    }

    override suspend fun onRejoin(event: ComponentInteractionCreateEvent, player: MultipleChoicePlayer) {
        event.interaction.respondEphemeral {
            content = translate(player, SongQuizTranslations.SongQuiz.Controls.rejoined)
        }
    }

    override suspend fun onJoin(ack: EphemeralMessageInteractionResponseBehavior, player: MultipleChoicePlayer) {
        val member = player.user.asMember(thread.guild.id)
        val voiceState = member.getVoiceStateOrNull()
        if (musicPlayer.lastChannelId != null && voiceState?.channelId != musicPlayer.lastChannelId?.let(::Snowflake)) {
            ack.createEphemeralFollowup {
                content = translate(
                    player,
                    SongQuizTranslations.SongQuiz.Controls.notInVc,
                    "<#${musicPlayer.lastChannelId}>"
                )
            }
        }
    }

    override suspend fun runGame() {
        if (musicPlayer.playingTrack != null) {
            beforePlayerState = musicPlayer.toState()
        }
        if (musicPlayer.lastChannelId == null) {
            val channel = host.asMember(thread.guildId).getVoiceStateOrNull()?.channelId
            if (channel == null) {
                thread.createMessage {
                    content = translate(SongQuizTranslations.MusicQuiz.Start.noVc)
                }
                return
            }
            musicPlayer.connectAudio(channel)
        }
        started = true

        musicPlayer.updateMusicChannelState(true)
        doUpdateWelcomeMessage()
        musicPlayer.queue.clear()

        super.runGame()
    }

    override suspend fun end() {
        musicPlayer.updateMusicChannelState(false)
        if (!started) return
        val state = beforePlayerState
        if (state == null) {
            musicPlayer.disconnectAudio()
        } else {
            // restore player state from before the quiz
            launch {
                state.schedulerOptions.applyToPlayer(musicPlayer)
                musicPlayer.applyState(state)
            }
        }
        super.end()
    }

    override suspend fun MessageCreateBuilder.questionUI(question: TrackQuestion) {
        actionRow {
            interactionButton(ButtonStyle.Primary, "like") {
                emoji = DiscordPartialEmoji(name = Emojis.heart.unicode)
            }
        }
    }

    override suspend fun EmbedBuilder.addQuestion(question: TrackQuestion, hideCorrectAnswer: Boolean) {
        if (hideCorrectAnswer) {
            title = question.title
        } else {
            addTrack(question.track, this@SongQuizGame)
        }
    }

    override suspend fun ComponentInteractionCreateEvent.handle(question: TrackQuestion): Boolean {
        if (interaction.componentId == "like") {
            interaction.respondEphemeral {
                val likedSongs =
                    MusicQuizDatabase.likedSongs.findOneById(interaction.user.id) ?: LikedSongs(
                        interaction.user.id,
                        emptySet()
                    )
                MusicQuizDatabase.likedSongs.save(likedSongs.copy(songs = likedSongs.songs + question.track.toLikedSong()))
                content = translate(interaction.gamePlayer!!, SongQuizTranslations.SongQuiz.Game.likedSong)
            }
            return true
        }
        return false
    }
}

enum class GuessingMode {
    NAME,
    ARTIST
}

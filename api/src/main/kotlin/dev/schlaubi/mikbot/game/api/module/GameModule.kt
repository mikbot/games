package dev.schlaubi.mikbot.game.api.module

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.channel.TextChannel
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.commands.application.ApplicationCommandContext
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.Player
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Abstract module for a game module.
 *
 * @param P the [Player] type
 * @param G the [AbstractGame] type
 *
 * @see SubCommandModule
 */
abstract class GameModule<P : Player, G : AbstractGame<P>>(context: PluginContext) : SubCommandModule(context) {

    private val games = mutableMapOf<Snowflake, G>()

    @Suppress("UNCHECKED_CAST")
    val asType: GameModule<P, AbstractGame<P>> get() = this as GameModule<P, AbstractGame<P>>

    override val commandName: Key
        get() = name.toKey()

    final override val allowApplicationCommandInDMs: Boolean = false

    /**
     * The [BotUser] property for the games stats.
     */
    abstract val gameStats: CoroutineCollection<UserGameStats>

    /**
     * The [ThreadChannelBehavior] of this [ApplicationCommandContext]
     */
    val ApplicationCommandContext.textChannel: TextChannel
        get() = runBlocking { channel.asChannel() as TextChannel }

    @OptIn(PrivilegedIntent::class)
    final override suspend fun overrideSetup() {
        intents.add(Intent.GuildMembers)
        gameSetup()
    }

    /**
     * Finds a game by its [threadId].
     */
    fun findGame(threadId: Snowflake): G? = games[threadId]

    /**
     * Registers [game] as the game for [threadId].
     */
    fun registerGame(
        threadId: Snowflake,
        game: G
    ) {
        games[threadId] = game
    }

    /**
     * Removes the game by its [threadId].
     */
    fun unregisterGame(threadId: Snowflake) = games.remove(threadId)

    /**
     * Additional setup calls.
     */
    protected open suspend fun gameSetup() = Unit
}

package dev.schlaubi.mikbot.game.api.module.commands

import dev.kordex.core.checks.isNotInThread
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.PublicSlashCommandContext
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.embed
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.game.api.*
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.games.translations.GameApiTranslations
import dev.schlaubi.mikbot.plugin.api.util.translate

/**
 * Adds a /start command to this game.
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 * @param arguments the argument body for this command
 * @param makeNewGame a lambda creating a new game
 */
fun <G : AbstractGame<*>> GameModule<*, G>.startGameCommand(
    gameTitleKey: Key,
    threadName: String,
    makeNewGame: suspend PublicSlashCommandContext<Arguments, *>.(gameMessage: Message, gameThread: ThreadChannelBehavior) -> G?,
    additionalChecks: suspend CheckContext<InteractionCreateEvent>.() -> Unit = {},
    name: Key = GameApiTranslations.Commands.Start.name,
    description: Key = GameApiTranslations.Commands.Start.description
) = startGameCommand(
    gameTitleKey,
    threadName,
    ::Arguments,
    makeNewGame,
    additionalChecks,
    name,
    description
)

/**
 * Adds a /start command to this game.
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 * @param arguments the argument body for this command
 * @param makeNewGame a lambda creating a new game
 */
fun <A : Arguments, G : AbstractGame<*>> GameModule<*, G>.startGameCommand(
    gameTitleKey: Key,
    threadName: String,
    arguments: () -> A,
    makeNewGame: suspend PublicSlashCommandContext<A, *>.(gameMessage: Message, gameThread: ThreadChannelBehavior) -> G?,
    additionalChecks: suspend CheckContext<InteractionCreateEvent>.() -> Unit = {},
    name: Key = GameApiTranslations.Commands.Start.name,
    description: Key = GameApiTranslations.Commands.Start.description
) = startGameCommand(
    gameTitleKey,
    threadName,
    arguments,
    { },
    { _, gameMessage, gameThread -> makeNewGame(gameMessage, gameThread) },
    additionalChecks,
    name,
    description
)

/**
 * Adds a /start command to this game.
 *
 * @param gameTitleKey the translation key for the embed title
 * @param threadName the thread name of the game thread
 * @param arguments the argument body for this command
 * @param prepareData a callback preparing [Data] before creating the thread
 * @param makeNewGame a lambda creating a new game
 */
fun <A : Arguments, G : AbstractGame<*>, Data> GameModule<*, G>.startGameCommand(
    gameTitleKey: Key,
    threadName: String,
    arguments: () -> A,
    prepareData: suspend PublicSlashCommandContext<A, *>.() -> Data?,
    makeNewGame: suspend PublicSlashCommandContext<A, *>.(data: Data, gameMessage: Message, gameThread: ThreadChannelBehavior) -> G?,
    additionalChecks: suspend CheckContext<InteractionCreateEvent>.() -> Unit = {},
    name: Key = GameApiTranslations.Commands.Start.name,
    description: Key = GameApiTranslations.Commands.Start.description
) = publicSubCommand(arguments) {
    this.name = name
    this.description = description

    check {
        isNotInThread()
        // Required for pin()
        requireBotPermissions(Permission.ManageMessages, Permission.ManageThreads, Permission.CreatePublicThreads)
        additionalChecks()
    }

    action {
        val data = prepareData() ?: return@action
        val gameThread = textChannel.startPublicThread(threadName) {}
        val gameMessage = gameThread.createMessage {
            embed {
                title = translate(gameTitleKey)

                footer {
                    text = translate(GameApiTranslations.Game.Header.footer)
                }
            }
        }

        gameMessage.pin(reason = "Game Welcome message")
        val game = makeNewGame(data, gameMessage, gameThread) ?: return@action

        fun <T : Player> AutoJoinableGame<T>.addPlayer(player: User) = players.add(obtainNewPlayer(player))

        if (game is AutoJoinableGame<*> || (game as? ControlledGame<*>)?.supportsAutoJoin == true) {
            gameThread.addUser(user.id) // Add creator
        }
        if (game is AutoJoinableGame<*>) {
            game.addPlayer(user.asUser())
        }
        game.doUpdateWelcomeMessage()
        registerGame(gameThread.id, game)
        respond {
            content = translate(GameApiTranslations.Game.Start.success)
        }
    }
}

package dev.schlaubi.mikbot.game.api

import dev.kord.common.asJavaLocale
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.getLocale
import java.util.*

suspend fun Game<*>.confirmation(
    ack: MessageInteractionResponseBehavior,
    hasNoOption: Boolean = true,
    locale: Locale? = null,
    messageBuilder: MessageBuilder
) =
    dev.schlaubi.mikbot.plugin.api.util.confirmation(
        {
            ack.createEphemeralFollowup {
                it()
            }
        },
        hasNoOption = hasNoOption,
        messageBuilder = messageBuilder,
        translatableContext = object : TranslatableContext {
            override var resolvedLocale: Locale? = locale ?: translationsProvider.defaultLocale
            override suspend fun getLocale(): Locale = locale ?: translationsProvider.defaultLocale

        }
    )

suspend fun ControlledPlayer.confirmation(
    hasNoOption: Boolean = true,
    messageBuilder: MessageBuilder
) = game.confirmation(ack, hasNoOption, locale, messageBuilder)

suspend fun ControlledPlayer.translate(key: Key, vararg replacements: Any?) =
    game.translate(key.withLocale(discordLocale?.asJavaLocale()), *replacements)

suspend fun <T : Player> AbstractGame<T>.update(
    player: T,
    updaterFunction: GameStats.() -> GameStats
) {
    val userStats =
        module.gameStats.findOneById(player.user.id) ?: UserGameStats(player.user.id, GameStats(0, 0, 0.0, 0))
    val newStats = userStats.copy(stats = userStats.stats.updaterFunction())

    module.gameStats.save(newStats)
}

/**
 * Translates [key] for a game.
 */
@Suppress("UNCHECKED_CAST")
suspend fun Game<*>.translate(
    key: Key,
    vararg replacements: Any?,
    locale: Locale? = null
) =
    translationsProvider.translate(
        key.withLocale(locale ?: locale(), overwrite = true),
        replacements = replacements as Array<Any?>
    )

suspend fun Game<*>.translate(user: Player, key: Key, vararg replacements: Any?): String =
    translate(key, locale = getLocale(user), replacements = replacements)

private suspend fun Game<*>.getLocale(user: Player) =
    (user as? ControlledPlayer)?.locale ?: module.bot.getLocale(
        thread.asChannel(),
        user.user.asUser()
    )
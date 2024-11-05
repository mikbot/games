package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.interaction.followup.FollowupMessageBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.utils.waitFor
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder

suspend fun DiscordUnoPlayer.translate(key: Key, vararg replacements: Any?) =
    game.translate(this, key, *replacements)

suspend fun DiscordUnoPlayer.awaitResponse(
    doneTranslationKey: Key,
    messageBuilder: MessageBuilder
): String? {
    val message = response.createEphemeralFollowup {
        messageBuilder()
    }

    val response = awaitResponse { message } ?: return null

    message.edit {
        components = mutableListOf()
        content = translate(doneTranslationKey)
    }

    return response
}

suspend fun DiscordUnoPlayer.awaitResponse(message: () -> FollowupMessageBehavior): String? {

    val response = game.kord.waitFor<ComponentInteractionCreateEvent>(unoInteractionTimeout) {
        interaction.message.id == message().id && interaction.user == user
    } ?: return null

    val ack = response.interaction.deferEphemeralMessageUpdate()
    this.response = ack

    return response.interaction.componentId
}

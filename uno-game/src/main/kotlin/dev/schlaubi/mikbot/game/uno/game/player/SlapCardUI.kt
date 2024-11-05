package dev.schlaubi.mikbot.game.uno.game.player

import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import dev.schlaubi.mikbot.plugin.api.util.MessageBuilder
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.uno.cards.SlapContext
import java.util.*

suspend fun DiscordUnoPlayer.openSlapCardUI(slapContext: SlapContext) {
    val messageBuilder: MessageBuilder = {
        content = translate(UnoTranslations.Game.Ui.SlapCard.description)
    }

    val (slapped) = confirmation(
        {
            controls.edit {
                val builder = FollowupMessageCreateBuilder(false)
                builder.it()

                content = builder.content
                components = builder.components
            }
        },
        hasNoOption = false,
        messageBuilder = messageBuilder,
        translatableContext = object : TranslatableContext {
            override var resolvedLocale: Locale? = locale
            override suspend fun getLocale(): Locale = locale!!
        },
        yesWord = translate(UnoTranslations.Game.Ui.SlapCard.slap),
        timeout = null
    )

    if (slapped) {
        slapContext.slap(this)
        updateControls(false) // show cards again
    }
}

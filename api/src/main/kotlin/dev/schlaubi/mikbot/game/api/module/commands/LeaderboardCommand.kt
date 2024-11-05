package dev.schlaubi.mikbot.game.api.module.commands

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.game.api.GameStats
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.games.translations.GameApiTranslations
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import org.litote.kmongo.descending
import org.litote.kmongo.div

/**
 * Adds a /leaderboard command to this [GameModule].
 * @param leaderboardTitleKey the translation key for the embed title
 */
@OptIn(KordUnsafe::class, KordExperimental::class)
fun GameModule<*, *>.leaderboardCommand(
    leaderboardTitleKey: Key
) = publicSubCommand {
    name = GameApiTranslations.Commands.Leaderboard.name
    description = GameApiTranslations.Commands.Leaderboard.description

    action {
        val count = gameStats.countDocuments()
        val all = gameStats.find()
            .sort(
                descending(
                    UserGameStats::stats / GameStats::totalGamesPlayed,
                    UserGameStats::stats / GameStats::ratio
                )
            )
            .toList()

        editingPaginator {
            forList(
                user,
                all,
                { (userId, stats) ->
                    val ratio = stats.ratio.formatPercentage()
                    val user = user.kord.unsafe.user(userId)

                    "${
                    user.asMemberOrNull(safeGuild.id)?.mention ?: user.asUserOrNull()?.username
                        ?: user.mention
                    } - ${stats.wins}/${stats.losses} ($ratio)"
                },
                { current: Int, total: Int ->
                    translate(
                        leaderboardTitleKey,
                        arrayOf(current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}

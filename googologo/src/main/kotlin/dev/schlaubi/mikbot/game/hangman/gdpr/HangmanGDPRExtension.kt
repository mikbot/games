package dev.schlaubi.mikbot.game.hangman.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.hangman.HangmanDatabase
import dev.schlaubi.mikbot.games.translations.HangmanTranslations
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class HangmanGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(UnoStatsDataPoint)

    object UnoStatsDataPoint : GameStatisticsDataPoint(HangmanTranslations.Gdpr.Stats.name, HangmanTranslations.Gdpr.Stats.description) {
        override val collection: CoroutineCollection<UserGameStats> = HangmanDatabase.stats
    }
}

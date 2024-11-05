package dev.schlaubi.mikbot.game.trivia.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.trivia.TriviaDatabase
import dev.schlaubi.mikbot.games.translations.TriviaTranslations
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class TriviaGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(TriviaStatsDataPoint, TriviaProcessDataPoint)
}

object TriviaStatsDataPoint : GameStatisticsDataPoint(TriviaTranslations.Gdpr.Stats.name, TriviaTranslations.Gdpr.Stats.description) {
    override val collection: CoroutineCollection<UserGameStats> = TriviaDatabase.stats
}

val TriviaProcessDataPoint = ProcessedData( TriviaTranslations.Gdpr.ProcessedData.description, null)

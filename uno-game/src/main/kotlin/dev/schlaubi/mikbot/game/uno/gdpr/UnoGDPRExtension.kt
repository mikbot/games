package dev.schlaubi.mikbot.game.uno.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.uno.UnoDatabase
import dev.schlaubi.mikbot.games.translations.UnoTranslations
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class UnoGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(UnoStatsDataPoint, UnoProcessDataPoint)
}

object UnoStatsDataPoint :
    GameStatisticsDataPoint(UnoTranslations.Gdpr.Stats.name, UnoTranslations.Gdpr.Stats.description) {
    override val collection: CoroutineCollection<UserGameStats> = UnoDatabase.stats
}

val UnoProcessDataPoint = ProcessedData(UnoTranslations.Gdpr.ProcessedData.description, null)

package dev.schlaubi.mikbot.game.tic_tac_toe.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.tic_tac_toe.TicTacToeDatabase
import dev.schlaubi.mikbot.games.translations.TicTacToeTranslations
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class TicTacToeGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(TicTacToeStatsDataPoint, TicTacToeProcessDataPoint)
}

object TicTacToeStatsDataPoint :
    GameStatisticsDataPoint(TicTacToeTranslations.Gdpr.Stats.name, TicTacToeTranslations.Gdpr.Stats.description) {
    override val collection: CoroutineCollection<UserGameStats> = TicTacToeDatabase.stats
}

val TicTacToeProcessDataPoint = ProcessedData(TicTacToeTranslations.Gdpr.ProcessedData.description, null)

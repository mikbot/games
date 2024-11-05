package dev.schlaubi.mikbot.game.tic_tac_toe.game

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key

@Suppress("EnumEntryName")
enum class GameSize(val size: Int, override val readableName: Key) : ChoiceEnum {
    `3_BY_3`(3, "3x3".toKey()),
    `4_BY_4`(4, "4x4".toKey()),
    `5_BY_5`(5, "5x5".toKey())
}

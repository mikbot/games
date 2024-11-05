package dev.schlaubi.uno

import java.util.*

/**
 * Polls [amount] items from this [LinkedList].
 */
public fun <T> LinkedList<T>.poll(amount: Int): List<T> = buildList(amount) {
    repeat(amount) {
        add(poll())
    }
}

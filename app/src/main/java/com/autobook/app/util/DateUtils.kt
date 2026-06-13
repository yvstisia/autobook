package com.autobook.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Indonesian locale used across the app for date display. */
private val indonesianLocale = Locale("id", "ID")
private const val DISPLAY_PATTERN = "dd MMM yyyy"

/** Formats an epoch-millis timestamp as e.g. "05 Jun 2026" using Indonesian locale. */
fun formatDate(epochMillis: Long): String =
    SimpleDateFormat(DISPLAY_PATTERN, indonesianLocale).format(Date(epochMillis))

/**
 * Returns the [start, end) epoch-millis boundaries of the calendar month that
 * contains [reference]. `start` is inclusive (first ms of the month), `end` is
 * exclusive (first ms of the next month).
 */
fun monthRange(reference: Long = System.currentTimeMillis()): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = reference
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}

/** Strips the time-of-day component, returning midnight of the given day. */
fun startOfDay(epochMillis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

/** Number of whole days from [fromMillis] to [toMillis] (positive if toMillis is later). */
fun daysBetween(fromMillis: Long, toMillis: Long): Long {
    val dayMs = 24L * 60 * 60 * 1000
    return (startOfDay(toMillis) - startOfDay(fromMillis)) / dayMs
}

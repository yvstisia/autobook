package com.autobook.app.util

/** Status of a service reminder relative to the current odometer / date. */
enum class ReminderStatus { ON_TRACK, DUE_SOON, OVERDUE }

private const val KM_THRESHOLD = 500
private const val DAYS_THRESHOLD = 14L

/**
 * Computes a [ReminderStatus] from a reminder's targets.
 *
 * Rules (per CLAUDE.md):
 *  - OVERDUE if past the target km OR past the target date.
 *  - DUE_SOON if within 500 km OR within 14 days of a target.
 *  - ON_TRACK otherwise.
 *
 * Any null target is simply ignored; if both are null the result is ON_TRACK.
 */
fun computeReminderStatus(
    currentOdometer: Int,
    nextKm: Int?,
    nextDate: Long?,
    today: Long = System.currentTimeMillis()
): ReminderStatus {
    var dueSoon = false

    nextKm?.let { target ->
        when {
            currentOdometer >= target -> return ReminderStatus.OVERDUE
            target - currentOdometer <= KM_THRESHOLD -> dueSoon = true
        }
    }

    nextDate?.let { target ->
        val days = daysBetween(today, target)
        when {
            days < 0 -> return ReminderStatus.OVERDUE
            days <= DAYS_THRESHOLD -> dueSoon = true
        }
    }

    return if (dueSoon) ReminderStatus.DUE_SOON else ReminderStatus.ON_TRACK
}

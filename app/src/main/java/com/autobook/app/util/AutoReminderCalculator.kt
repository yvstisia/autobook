package com.autobook.app.util

import com.autobook.app.data.local.entity.ServiceReminder
import java.util.Calendar

/**
 * Generates service reminders automatically from a logged service, using conservative
 * Indonesian-standard intervals. Only predictable service types (oil, tune-up, battery)
 * produce reminders; tires/brakes/other are history-only and produce none.
 *
 * Intervals (tune here without touching any other code):
 *  - Oil:     motor +2.000 km / +2 mo,   mobil +5.000 km / +6 mo   (remind by both)
 *  - Tune-up: motor +8.000 km / +6 mo,   mobil +10.000 km / +6 mo  (remind by both)
 *  - Battery: motor +18 mo,              mobil +24 mo              (remind by date)
 */
object AutoReminderCalculator {

    /** Service types that can produce a reminder. Others are history-only. */
    val PREDICTABLE_TYPES = setOf("oli", "tune_up", "aki")

    private data class Interval(val kmDelta: Int?, val monthsDelta: Int, val remindBy: String)

    /**
     * Builds reminders for the predictable types among [selectedTypes]. The returned rows
     * have serviceRecordId = 0; the caller links them to the inserted record's id.
     */
    fun generate(
        vehicleType: String,
        vehicleId: Int,
        serviceDate: Long,
        odometerAtService: Int,
        selectedTypes: Collection<String>
    ): List<ServiceReminder> {
        val isMobil = vehicleType == "mobil"
        return selectedTypes.mapNotNull { type ->
            val interval = intervalFor(type, isMobil) ?: return@mapNotNull null
            ServiceReminder(
                serviceRecordId = 0,
                vehicleId = vehicleId,
                serviceType = type,
                remindBy = interval.remindBy,
                nextKm = interval.kmDelta?.let { odometerAtService + it },
                nextDate = addMonths(serviceDate, interval.monthsDelta)
            )
        }
    }

    private fun intervalFor(type: String, isMobil: Boolean): Interval? = when (type) {
        "oli" -> if (isMobil) Interval(5000, 6, "both") else Interval(2000, 2, "both")
        "tune_up" -> if (isMobil) Interval(10000, 6, "both") else Interval(8000, 6, "both")
        "aki" -> if (isMobil) Interval(null, 24, "date") else Interval(null, 18, "date")
        else -> null // ban, rem, lainnya -> history only
    }

    private fun addMonths(epochMillis: Long, months: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.MONTH, months)
        }.timeInMillis
}

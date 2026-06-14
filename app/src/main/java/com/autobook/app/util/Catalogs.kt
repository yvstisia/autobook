package com.autobook.app.util

import androidx.annotation.StringRes
import com.autobook.app.R

/** A stored code value paired with its display label resource. */
data class CodedLabel(val code: String, @StringRes val labelRes: Int)

/** Service type options. `code` is what is persisted in ServiceRecord.serviceTypes. */
val serviceTypeOptions = listOf(
    CodedLabel("oli", R.string.service_type_oli),
    CodedLabel("tune_up", R.string.service_type_tune_up),
    CodedLabel("ban", R.string.service_type_ban),
    CodedLabel("rem", R.string.service_type_rem),
    CodedLabel("aki", R.string.service_type_aki),
    CodedLabel("lainnya", R.string.service_type_lainnya)
)

/** Workshop specialization options. `code` is persisted in Workshop.specialization. */
val specializationOptions = listOf(
    CodedLabel("oli", R.string.spec_oli),
    CodedLabel("ban", R.string.spec_ban),
    CodedLabel("listrik", R.string.spec_listrik),
    CodedLabel("body", R.string.spec_body),
    CodedLabel("umum", R.string.spec_umum)
)

/** Fuel types (stored verbatim in FuelLog.fuelType). */
val fuelTypes = listOf("Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite")

/**
 * Max digits allowed in an odometer / km-target input. 6 digits (up to 999.999 km) covers
 * motorcycles and cars while blocking absurd or typo values.
 */
const val ODOMETER_MAX_DIGITS = 6

/** "remindBy" options paired with display labels. */
val remindByOptions = listOf(
    CodedLabel("km", R.string.remind_by_km),
    CodedLabel("date", R.string.remind_by_date),
    CodedLabel("both", R.string.remind_by_both)
)

/** Joins selected codes into the comma-separated form stored in the DB. */
fun joinCodes(codes: Collection<String>): String = codes.joinToString(",")

/** Splits a stored comma-separated code string back into a set (empties removed). */
fun splitCodes(stored: String): Set<String> =
    stored.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

package com.autobook.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Number formatting per DESIGN.md §2 — Indonesian conventions:
// dot thousands separator, comma decimal separator.
private val idLocale = Locale("id", "ID")
private val idSymbols = DecimalFormatSymbols(idLocale).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}
private val integerFormat = DecimalFormat("#,###", idSymbols)
private val oneDecimalFormat = DecimalFormat("#,##0.0", idSymbols)

/** "Rp 45.000" — dot separator, no decimals. */
fun formatRupiah(amount: Int): String = "Rp " + integerFormat.format(amount.toLong())

/** "54.854" — odometer figure without unit; render "km" separately in labelSmall. */
fun formatOdometer(km: Int): String = integerFormat.format(km.toLong())

/** "42,5 km/L" — one decimal, comma separator; "—" when 0 (first entry / no data). */
fun formatKmPerLiter(value: Float): String =
    if (value <= 0f) "—" else oneDecimalFormat.format(value.toDouble()) + " km/L"

/** "4,5 L" — liters with one decimal, comma separator. */
fun formatLiters(value: Float): String = oneDecimalFormat.format(value.toDouble()) + " L"

/** "Kamis, 11 Jun" — dashboard header date. */
fun formatDayDate(epochMillis: Long): String =
    SimpleDateFormat("EEEE, dd MMM", idLocale).format(Date(epochMillis))

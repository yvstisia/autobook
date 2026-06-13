package com.autobook.app.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.autobook.app.data.preferences.AppCurrency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Locale- and currency-aware display formatting. The active [locale] drives grouping and
 * decimal separators plus month names; the active [currency] drives the symbol and the
 * number of fraction digits. Money is stored as INTEGER minor units (see [AppCurrency]).
 *
 * Provided app-wide via [LocalAppFormatter], seeded at the root from user preferences and
 * the current resource configuration locale (which AppCompat keeps in sync with the
 * chosen language).
 */
class AppFormatter(
    val currency: AppCurrency,
    val locale: Locale
) {
    private val symbols = DecimalFormatSymbols(locale)
    private val integerFormat = DecimalFormat("#,##0", symbols)
    private val oneDecimalFormat = DecimalFormat("#,##0.0", symbols)
    private val moneyFormat = DecimalFormat(
        if (currency.decimalPlaces > 0) "#,##0." + "0".repeat(currency.decimalPlaces) else "#,##0",
        symbols
    )

    /** Stored minor-unit integer -> currency string. 45000 IDR -> "Rp 45.000"; 4550 USD -> "$45.50". */
    fun money(minorUnits: Int): String {
        val major = minorUnits.toDouble() / currency.minorUnitFactor
        val number = moneyFormat.format(major)
        val space = if (currency.spaceAfterSymbol) " " else ""
        return if (currency.symbolBeforeAmount) "${currency.symbol}$space$number"
        else "$number$space${currency.symbol}"
    }

    /** Odometer figure without unit, e.g. "54.854" (id) / "54,854" (en). */
    fun odometer(km: Int): String = integerFormat.format(km.toLong())

    /** "42,5 km/L" / "42.5 km/L"; "—" when 0 (first entry / no data). */
    fun kmPerLiter(value: Float): String =
        if (value <= 0f) "—" else oneDecimalFormat.format(value.toDouble()) + " km/L"

    /** "4,5 L" / "4.5 L". */
    fun liters(value: Float): String = oneDecimalFormat.format(value.toDouble()) + " L"

    /** "05 Jun 2026" with locale month names. */
    fun date(epochMillis: Long): String =
        SimpleDateFormat("dd MMM yyyy", locale).format(Date(epochMillis))

    /** "Kamis, 11 Jun" / "Thursday, 11 Jun" — dashboard header. */
    fun dayDate(epochMillis: Long): String =
        SimpleDateFormat("EEEE, dd MMM", locale).format(Date(epochMillis))

    /**
     * Parse user money input (major units, possibly with locale separators) into minor units
     * for storage. Returns null when the input is not a valid number.
     */
    fun parseMoneyToMinorUnits(input: String): Int? {
        val major = normalizeNumber(input).toDoubleOrNull() ?: return null
        return (major * currency.minorUnitFactor).roundToInt()
    }

    companion object {
        /**
         * Normalize locale-flavored numeric input to a plain parseable form. The last '.' or ','
         * is treated as the decimal point; any earlier separators are stripped as grouping.
         * "1.234,50" and "1,234.50" both -> "1234.50".
         */
        fun normalizeNumber(input: String): String {
            val cleaned = input.trim().filter { it.isDigit() || it == '.' || it == ',' }
            if (cleaned.isEmpty()) return ""
            val decimalIdx = maxOf(cleaned.lastIndexOf('.'), cleaned.lastIndexOf(','))
            if (decimalIdx == -1) return cleaned
            val intPart = cleaned.substring(0, decimalIdx).filter { it.isDigit() }
            val fracPart = cleaned.substring(decimalIdx + 1).filter { it.isDigit() }
            return if (fracPart.isEmpty()) intPart else "$intPart.$fracPart"
        }
    }
}

/** App-wide formatter. Default (IDR / Indonesian) until the root provides the real one. */
val LocalAppFormatter = staticCompositionLocalOf {
    AppFormatter(AppCurrency.IDR, Locale("id", "ID"))
}

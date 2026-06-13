package com.autobook.app.data.preferences

/**
 * Supported display/recording currencies. There is NO exchange-rate conversion —
 * whatever currency is active is the currency amounts are entered and stored in.
 *
 * Money is stored as INTEGER minor units (e.g. cents). [decimalPlaces] tells the
 * formatter how many fraction digits a major amount has and the divisor (10^places)
 * for converting stored minor units back to a major value:
 *   IDR/JPY -> 0 places, minor unit == major unit (45000 -> "Rp 45.000")
 *   USD/EUR -> 2 places, 100 minor units == 1 major  (4550  -> "$45.50")
 */
enum class AppCurrency(
    val code: String,
    val symbol: String,
    val symbolBeforeAmount: Boolean,
    val spaceAfterSymbol: Boolean,
    val decimalPlaces: Int
) {
    IDR("IDR", "Rp", true, true, 0),
    USD("USD", "$", true, false, 2),
    EUR("EUR", "€", true, false, 2),
    GBP("GBP", "£", true, false, 2),
    SGD("SGD", "S$", true, false, 2),
    MYR("MYR", "RM", true, false, 2),
    JPY("JPY", "¥", true, false, 0),
    INR("INR", "₹", true, false, 2),
    AUD("AUD", "A$", true, false, 2);

    /** Number of minor units in one major unit, e.g. 100 for USD, 1 for IDR. */
    val minorUnitFactor: Int
        get() {
            var factor = 1
            repeat(decimalPlaces) { factor *= 10 }
            return factor
        }

    companion object {
        fun fromCode(value: String?): AppCurrency =
            entries.firstOrNull { it.code == value } ?: IDR
    }
}

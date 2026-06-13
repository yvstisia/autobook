package com.autobook.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.autobook.app.data.preferences.AppLanguage

/**
 * Applies the chosen [AppLanguage] as the per-app locale using AppCompat's backport, which
 * works down to API 26 and persists the choice across restarts. SYSTEM clears the override
 * so the app follows the OS language.
 */
object LocaleManager {
    fun apply(language: AppLanguage) {
        val locales = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}

package com.autobook.app.data.preferences

/**
 * Theme preference. SYSTEM follows the OS dark-mode setting; LIGHT/DARK force one.
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/**
 * Language preference. [tag] is the BCP-47 language tag used with
 * AppCompatDelegate.setApplicationLocales(); SYSTEM uses an empty list (follow OS).
 */
enum class AppLanguage(val tag: String) {
    SYSTEM(""), INDONESIAN("id"), ENGLISH("en");

    companion object {
        fun fromName(value: String?): AppLanguage =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

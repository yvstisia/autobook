package com.autobook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobook.app.data.preferences.AppCurrency
import com.autobook.app.data.preferences.AppLanguage
import com.autobook.app.data.preferences.ThemeMode
import com.autobook.app.data.preferences.UserPreferencesRepository
import com.autobook.app.util.LocaleManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs both the Settings screen and the first-run Onboarding screen — they read and write
 * the same user preferences. Setters persist via [UserPreferencesRepository]; changing the
 * language also applies the per-app locale immediately through [LocaleManager].
 */
class SettingsViewModel(private val prefs: UserPreferencesRepository) : ViewModel() {

    val userName: StateFlow<String> =
        prefs.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val themeMode: StateFlow<ThemeMode> =
        prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val language: StateFlow<AppLanguage> =
        prefs.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.SYSTEM)

    val currency: StateFlow<AppCurrency> =
        prefs.currency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppCurrency.IDR)

    fun setUserName(value: String) {
        viewModelScope.launch { prefs.setUserName(value) }
    }

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(value) }
    }

    fun setLanguage(value: AppLanguage) {
        viewModelScope.launch {
            prefs.setLanguage(value)
            LocaleManager.apply(value)
        }
    }

    fun setCurrency(value: AppCurrency) {
        viewModelScope.launch { prefs.setCurrency(value) }
    }

    /** Marks first-run onboarding complete. */
    fun completeOnboarding() {
        viewModelScope.launch { prefs.setHasOnboarded(true) }
    }
}

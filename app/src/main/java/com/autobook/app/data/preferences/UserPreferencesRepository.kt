package com.autobook.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Single app-wide DataStore instance, scoped to the application Context.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Persists user-facing app preferences (profile name, theme, language, currency, and the
 * one-time onboarding flag) using Jetpack DataStore. Exposes each value as a [Flow] and
 * provides suspend setters. Created once and held by [com.autobook.app.AppContainer].
 */
class UserPreferencesRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY = stringPreferencesKey("currency")
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
    }

    val userName: Flow<String> =
        dataStore.data.map { it[Keys.USER_NAME] ?: "" }

    val themeMode: Flow<ThemeMode> =
        dataStore.data.map { ThemeMode.fromName(it[Keys.THEME_MODE]) }

    val language: Flow<AppLanguage> =
        dataStore.data.map { AppLanguage.fromName(it[Keys.LANGUAGE]) }

    val currency: Flow<AppCurrency> =
        dataStore.data.map { AppCurrency.fromCode(it[Keys.CURRENCY]) }

    val hasOnboarded: Flow<Boolean> =
        dataStore.data.map { it[Keys.HAS_ONBOARDED] ?: false }

    suspend fun setUserName(value: String) {
        dataStore.edit { it[Keys.USER_NAME] = value }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setLanguage(value: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = value.name }
    }

    suspend fun setCurrency(value: AppCurrency) {
        dataStore.edit { it[Keys.CURRENCY] = value.code }
    }

    suspend fun setHasOnboarded(value: Boolean) {
        dataStore.edit { it[Keys.HAS_ONBOARDED] = value }
    }
}

package com.turkcell.lyraapp.data.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [ThemePreferencesRepository]'nin Jetpack Preferences DataStore tabanlı gerçek uygulaması.
 */
class ThemePreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemePreferencesRepository {

    override val isDarkTheme: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_THEME]
    }

    override suspend fun setDarkTheme(isDark: Boolean?) {
        dataStore.edit { preferences ->
            if (isDark == null) {
                preferences.remove(KEY_IS_DARK_THEME)
            } else {
                preferences[KEY_IS_DARK_THEME] = isDark
            }
        }
    }

    private companion object {
        val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }
}

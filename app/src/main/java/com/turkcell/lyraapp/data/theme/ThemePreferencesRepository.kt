package com.turkcell.lyraapp.data.theme

import kotlinx.coroutines.flow.Flow

/**
 * Tema tercihlerini saklamak ve okumak için kullanılan veri deposu arayüzü.
 */
interface ThemePreferencesRepository {
    /**
     * Koyu tema tercihini dinler.
     * null: Sistem varsayılanı, true: Koyu tema, false: Açık tema.
     */
    val isDarkTheme: Flow<Boolean?>

    /**
     * Koyu tema tercihini kaydeder.
     */
    suspend fun setDarkTheme(isDark: Boolean?)
}

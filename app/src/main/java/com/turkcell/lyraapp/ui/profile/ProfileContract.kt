package com.turkcell.lyraapp.ui.profile

/**
 * Profil ekranının MVI sözleşmesi.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false, // seçili olan tema
    val initials: String = "ZK",
    val name: String = "Zeynep Kaya",
    val handle: String = "@zeynepk",
    val status: String = "Premium",
    val playlistsCount: Int = 127,
    val followersCount: String = "1.2B",
    val followingCount: Int = 348
)

sealed interface ProfileIntent {
    data class ThemeChanged(val isDark: Boolean) : ProfileIntent
    data object SettingsClicked : ProfileIntent
    data class SettingItemClicked(val title: String) : ProfileIntent
}

sealed interface ProfileEffect {
    data object ShowSettingsMessage : ProfileEffect
}

package com.turkcell.lyraapp.ui.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false,
    val initials: String = "",
    val name: String = "",
    val handle: String = "",
    val status: String = "Premium",
    val playlistsCount: Int = 127,
    val followersCount: String = "1.2B",
    val followingCount: Int = 348
)

sealed interface ProfileIntent {
    data class ThemeChanged(val isDark: Boolean) : ProfileIntent
    data object SettingsClicked : ProfileIntent
    data class SettingItemClicked(val title: String) : ProfileIntent
    data object LogoutClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data object ShowSettingsMessage : ProfileEffect
    data object NavigateToLogin : ProfileEffect
}

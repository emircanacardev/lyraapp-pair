package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.ForYouSong
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val songs: List<HomeSong> = emptyList(),
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val forYouSongs: List<ForYouSong> = emptyList(),
    val isDarkTheme: Boolean = false,
)

sealed interface HomeIntent {
    data object Retry : HomeIntent
    data object ToggleTheme : HomeIntent
    data class SongSelected(val song: HomeSong) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data class NavigateToPlayer(
        val songId: String,
        val title: String,
        val artist: String,
        val startColor: Long,
        val endColor: Long,
    ) : HomeEffect
}

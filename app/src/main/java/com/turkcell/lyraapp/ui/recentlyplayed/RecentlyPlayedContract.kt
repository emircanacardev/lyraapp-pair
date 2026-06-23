package com.turkcell.lyraapp.ui.recentlyplayed

data class RecentlyPlayedUiState(
    val isLoading: Boolean = true,
    val songs: List<RecentlyPlayedSong> = emptyList(),
    val error: String? = null,
)

data class RecentlyPlayedSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

sealed interface RecentlyPlayedIntent {
    data class SongSelected(val song: RecentlyPlayedSong) : RecentlyPlayedIntent
    data object Retry : RecentlyPlayedIntent
}

sealed interface RecentlyPlayedEffect {
    data class NavigateToPlayer(
        val songId: String,
        val title: String,
        val artist: String,
        val startColor: Long,
        val endColor: Long,
    ) : RecentlyPlayedEffect
}

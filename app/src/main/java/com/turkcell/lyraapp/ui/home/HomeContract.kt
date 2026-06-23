package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.FeaturedPlaylist
import com.turkcell.lyraapp.data.home.ForYouSong
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.Recommendation
import com.turkcell.lyraapp.data.home.RecentlyPlayed

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val songs: List<HomeSong> = emptyList(),
    val playlists: List<FeaturedPlaylist> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val forYouSongs: List<ForYouSong> = emptyList(),
    val isDarkTheme: Boolean = false,
)

sealed interface HomeIntent {
    data object Retry : HomeIntent
    data object ToggleTheme : HomeIntent
    data object ShowAllRecentlyPlayed : HomeIntent
    data object AvatarClicked : HomeIntent
    data object RefreshRecentlyPlayed : HomeIntent
    data class SongSelected(val song: HomeSong) : HomeIntent
    data class RecentlyPlayedSelected(val song: RecentlyPlayed) : HomeIntent
    data class ForYouSongSelected(val song: ForYouSong) : HomeIntent
    data class RecommendationSelected(val song: Recommendation) : HomeIntent
    data class PlaylistSelected(val playlistId: String) : HomeIntent
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
    data class NavigateToPlaylist(val playlistId: String) : HomeEffect
    data object NavigateToAllRecentlyPlayed : HomeEffect
    data object NavigateToProfile : HomeEffect
}

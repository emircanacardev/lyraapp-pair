package com.turkcell.lyraapp.ui.favorites

import com.turkcell.lyraapp.data.favorites.Song

/**
 * Favoriler ekranının MVI sözleşmesi: State (durum), Intent (niyet) ve Effect (olay).
 */
data class FavoritesUiState(
    val isLoading: Boolean = false,
    val title: String = "Beğenilen Şarkılar",
    val subtitle: String = "5 şarkı · 19 dk",
    val songs: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val playingSongId: String? = "s-4", // Neon Sokaklar varsayılan olarak çalan şarkı
    val isDownloaded: Boolean = false,
    val isShuffled: Boolean = false
)

sealed interface FavoritesIntent {
    data object PlayClicked : FavoritesIntent
    data object ShuffleClicked : FavoritesIntent
    data object DownloadClicked : FavoritesIntent
    data class SongClicked(val songId: String) : FavoritesIntent
    data class FavoriteClicked(val songId: String) : FavoritesIntent
    data object Retry : FavoritesIntent
}

sealed interface FavoritesEffect {
    data class ShowError(val message: String) : FavoritesEffect
}

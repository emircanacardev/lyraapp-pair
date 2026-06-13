package com.turkcell.lyraapp.ui.library

data class LibraryUiState(
    val selectedFilter: LibraryFilter = LibraryFilter.Playlists,
    val playlists: List<PlaylistItem> = emptyList(),
)

enum class LibraryFilter(val label: String) {
    Playlists("Çalma listeleri"),
    Artists("Sanatçılar"),
    Albums("Albümler"),
}

data class PlaylistItem(
    val id: String,
    val title: String,
    val songCount: Int,
    val isPinned: Boolean,
    val thumbnailColor: Long,
    val isLikedSongs: Boolean = false,
)

sealed interface LibraryIntent {
    data class FilterSelected(val filter: LibraryFilter) : LibraryIntent
    data class PlaylistClicked(val id: String) : LibraryIntent
    data class MoreClicked(val id: String) : LibraryIntent
    data object SearchClicked : LibraryIntent
    data object AddClicked : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToSearch : LibraryEffect
    data object NavigateToCreatePlaylist : LibraryEffect
    data class OpenPlaylist(val id: String) : LibraryEffect
}

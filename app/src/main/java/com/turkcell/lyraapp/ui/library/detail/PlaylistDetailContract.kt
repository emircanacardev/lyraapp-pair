package com.turkcell.lyraapp.ui.library.detail

data class PlaylistDetailUiState(
    val playlistId: String = "",
    val title: String = "",
    val description: String = "",
    val ownerName: String = "",
    val songCount: Int = 0,
    val totalDuration: String = "",
    val coverColor: Long = 0xFF7B5EA7L,
    val isLiked: Boolean = false,
    val isLikedSongs: Boolean = false,
    val songs: List<SongItem> = emptyList(),
)

data class SongItem(
    val id: String,
    val title: String,
    val artistName: String,
    val duration: String,
    val thumbnailColor: Long,
    val isLiked: Boolean,
    val isPlaying: Boolean = false,
)

sealed interface PlaylistDetailIntent {
    data object BackClicked : PlaylistDetailIntent
    data object LikePlaylistClicked : PlaylistDetailIntent
    data object PlayClicked : PlaylistDetailIntent
    data object ShuffleClicked : PlaylistDetailIntent
    data class LikeSongClicked(val songId: String) : PlaylistDetailIntent
    data class SongClicked(val songId: String) : PlaylistDetailIntent
    data class MoreSongClicked(val songId: String) : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
}

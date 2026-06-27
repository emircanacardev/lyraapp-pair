package com.turkcell.lyraapp.ui.library.create

data class CreatePlaylistUiState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val coverColor: Long = 0xFFB5756CL,
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSongsLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableSongs: List<SelectableSong> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
)

data class SelectableSong(
    val id: String,
    val title: String,
    val artistName: String,
    val thumbnailColor: Long,
)

sealed interface CreatePlaylistIntent {
    data class NameChanged(val value: String) : CreatePlaylistIntent
    data class DescriptionChanged(val value: String) : CreatePlaylistIntent
    data object TogglePublic : CreatePlaylistIntent
    data class ToggleSong(val songId: String) : CreatePlaylistIntent
    data object SaveClicked : CreatePlaylistIntent
    data object CloseClicked : CreatePlaylistIntent
}

sealed interface CreatePlaylistEffect {
    data class NavigateBack(val playlistCreated: Boolean) : CreatePlaylistEffect
}

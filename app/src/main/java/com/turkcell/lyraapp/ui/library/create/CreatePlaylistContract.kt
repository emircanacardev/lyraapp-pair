package com.turkcell.lyraapp.ui.library.create

data class CreatePlaylistUiState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val selectedSongIds: Set<String> = emptySet(),
    val availableSongs: List<SelectableSong> = emptyList(),
    val coverColor: Long = 0xFFB5756CL,
    val isSaveEnabled: Boolean = false,
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
    data object NavigateBack : CreatePlaylistEffect
}

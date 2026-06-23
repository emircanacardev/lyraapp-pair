package com.turkcell.lyraapp.ui.player

data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val startColor: Long = 0xFF8B6FB8L,
    val endColor: Long = 0xFF4A3D6BL,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasEnded: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null,
)

sealed interface PlayerIntent {
    data object TogglePlayPause : PlayerIntent
    data object Restart : PlayerIntent
    data object SeekForward : PlayerIntent
    data object SeekBackward : PlayerIntent
    data class SeekTo(val positionMs: Long) : PlayerIntent
    data object Retry : PlayerIntent
}

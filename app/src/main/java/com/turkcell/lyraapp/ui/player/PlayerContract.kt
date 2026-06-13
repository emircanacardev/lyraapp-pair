package com.turkcell.lyraapp.ui.player

/**
 * Now Playing (Çalar) ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 */
data class PlayerUiState(
    val title: String = "",
    val subtitle: String = "",
    val startColor: Long = 0xFF000000L,
    val endColor: Long = 0xFF000000L,
    val isPlaying: Boolean = true,
    val isFavorite: Boolean = false,
    val progress: Float = 0.4099f, // 1:33 / 3:43 (~%41)
    val currentTime: String = "1:33",
    val duration: String = "3:43",
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
)

sealed interface PlayerIntent {
    data object TogglePlayPause : PlayerIntent
    data object ToggleFavorite : PlayerIntent
    data object ToggleShuffle : PlayerIntent
    data object ToggleRepeat : PlayerIntent
    data class ProgressChanged(val value: Float) : PlayerIntent
    data object NavigateBack : PlayerIntent
    data object SkipNext : PlayerIntent
    data object SkipPrevious : PlayerIntent
}

sealed interface PlayerEffect {
    data object NavigateBack : PlayerEffect
}

package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class PlayingTrack(
    val id: String = "",
    val title: String,
    val artist: String,
    val startColor: Long,
    val endColor: Long,
    val isPlaying: Boolean = true,
    val isFavorite: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
) {
    val progress: Float get() = if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f
}

@Singleton
class PlaybackManager @Inject constructor() {

    private val _playingTrack = MutableStateFlow<PlayingTrack?>(null)
    val playingTrack: StateFlow<PlayingTrack?> = _playingTrack.asStateFlow()

    fun setTrack(track: PlayingTrack) {
        _playingTrack.value = track
    }

    fun setPlaying(isPlaying: Boolean) {
        _playingTrack.update { it?.copy(isPlaying = isPlaying) }
    }

    fun updateProgress(positionMs: Long, durationMs: Long) {
        _playingTrack.update { it?.copy(positionMs = positionMs, durationMs = durationMs) }
    }

    fun toggleFavorite() {
        _playingTrack.update { it?.copy(isFavorite = !(it.isFavorite)) }
    }
}

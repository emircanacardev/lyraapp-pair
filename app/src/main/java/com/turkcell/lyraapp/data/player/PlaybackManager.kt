package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Şu an çalan şarkının durum modeli.
 */
data class PlayingTrack(
    val title: String,
    val subtitle: String,
    val startColor: Long,
    val endColor: Long,
    val isPlaying: Boolean = true,
    val isFavorite: Boolean = false,
    val progress: Float = 0.4099f, // 1:33 / 3:43 (~%41)
    val currentTime: String = "1:33",
    val duration: String = "3:43",
)

/**
 * Uygulama genelinde çalma durumunu (playback state) yöneten Singleton sınıfı.
 *
 * Hem mini çalar (MiniPlayer) hem de tam ekran çalar (PlayerScreen) bu durumdan beslenir.
 */
@Singleton
class PlaybackManager @Inject constructor() {

    private val _playingTrack = MutableStateFlow<PlayingTrack?>(null)
    val playingTrack: StateFlow<PlayingTrack?> = _playingTrack.asStateFlow()

    fun playTrack(title: String, subtitle: String, startColor: Long, endColor: Long) {
        _playingTrack.value = PlayingTrack(
            title = title,
            subtitle = subtitle,
            startColor = startColor,
            endColor = endColor,
            isPlaying = true
        )
    }

    fun togglePlayPause() {
        _playingTrack.update { current ->
            current?.copy(isPlaying = !current.isPlaying)
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        _playingTrack.update { current ->
            current?.copy(isPlaying = isPlaying)
        }
    }

    fun toggleFavorite() {
        _playingTrack.update { current ->
            current?.copy(isFavorite = !current.isFavorite)
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        _playingTrack.update { current ->
            current?.copy(isFavorite = isFavorite)
        }
    }

    fun setProgress(progress: Float) {
        _playingTrack.update { current ->
            if (current == null) return@update null
            // 3:43 -> 223 saniye üzerinden zaman dökümü hesapla
            val totalSeconds = 223
            val currentSeconds = (totalSeconds * progress).toInt()
            val minutes = currentSeconds / 60
            val seconds = currentSeconds % 60
            val timeString = String.format("%d:%02d", minutes, seconds)
            current.copy(
                progress = progress,
                currentTime = timeString
            )
        }
    }

    fun skipNext() {
        // Mock davranışı: Şarkı sonuna veya sonraki şarkı mock verisine geçilebilir.
        // Şimdilik sadece çalma süresini sıfırlayalım veya benzeri mock geçiş yapalım.
        _playingTrack.update { current ->
            current?.copy(
                progress = 0.0f,
                currentTime = "0:00"
            )
        }
    }

    fun skipPrevious() {
        _playingTrack.update { current ->
            current?.copy(
                progress = 0.0f,
                currentTime = "0:00"
            )
        }
    }
}

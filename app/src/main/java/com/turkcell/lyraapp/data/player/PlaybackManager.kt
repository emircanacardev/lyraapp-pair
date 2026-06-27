package com.turkcell.lyraapp.data.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.service.LyraMediaService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus= */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playingTrack = MutableStateFlow<PlayingTrack?>(null)
    val playingTrack: StateFlow<PlayingTrack?> = _playingTrack.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playingTrack.update { it?.copy(isPlaying = isPlaying) }
            }
        })
        startProgressLoop()
    }

    private fun startProgressLoop() {
        scope.launch {
            while (true) {
                if (player.isPlaying) {
                    _playingTrack.update {
                        it?.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0L),
                            durationMs = player.duration.coerceAtLeast(0L),
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    fun setTrack(track: PlayingTrack) {
        _playingTrack.value = track
        LyraMediaService.start(context)
    }

    fun setPlaying(isPlaying: Boolean) {
        if (isPlaying) player.play() else player.pause()
    }

    fun updateProgress(positionMs: Long, durationMs: Long) {
        _playingTrack.update { it?.copy(positionMs = positionMs, durationMs = durationMs) }
    }

    fun toggleFavorite() {
        _playingTrack.update { it?.copy(isFavorite = !(it.isFavorite)) }
    }
}

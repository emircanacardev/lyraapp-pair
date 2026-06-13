package com.turkcell.lyraapp.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.turkcell.lyraapp.data.player.PlaybackManager

/**
 * Now Playing (Çalar) ekranının MVI ViewModel sınıfı.
 *
 * SavedStateHandle aracılığıyla navigasyondan gelen "title", "subtitle", "startColor"
 * ve "endColor" argümanlarını okur ve başlangıç durumu olarak atar.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playbackManager: PlaybackManager,
) : ViewModel() {

    private val title: String = savedStateHandle["title"] ?: "Bilinmeyen Şarkı"
    private val subtitle: String = savedStateHandle["subtitle"] ?: "Bilinmeyen Sanatçı"
    private val startColor: Long = savedStateHandle["startColor"] ?: 0xFF8B6FB8L
    private val endColor: Long = savedStateHandle["endColor"] ?: 0xFF4A3D6BL

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            title = title,
            subtitle = subtitle,
            startColor = startColor,
            endColor = endColor,
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlayerEffect>(Channel.BUFFERED)
    val effect: Flow<PlayerEffect> = _effect.receiveAsFlow()

    init {
        // Eğer merkezi çalma durumu boşsa veya gelen argümanlar farklıysa durumu güncelle/başlat
        val currentTrack = playbackManager.playingTrack.value
        if (currentTrack == null || currentTrack.title != title || currentTrack.subtitle != subtitle) {
            playbackManager.playTrack(
                title = title,
                subtitle = subtitle,
                startColor = startColor,
                endColor = endColor
            )
        }

        // Merkezi çalma durumunu uiState'e bağla
        viewModelScope.launch {
            playbackManager.playingTrack.collect { track ->
                if (track != null) {
                    _uiState.update { current ->
                        current.copy(
                            title = track.title,
                            subtitle = track.subtitle,
                            startColor = track.startColor,
                            endColor = track.endColor,
                            isPlaying = track.isPlaying,
                            isFavorite = track.isFavorite,
                            progress = track.progress,
                            currentTime = track.currentTime,
                            duration = track.duration
                        )
                    }
                }
            }
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.TogglePlayPause -> {
                playbackManager.togglePlayPause()
            }
            is PlayerIntent.ToggleFavorite -> {
                playbackManager.toggleFavorite()
            }
            is PlayerIntent.ToggleShuffle -> {
                _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
            }
            is PlayerIntent.ToggleRepeat -> {
                _uiState.update { it.copy(isRepeatEnabled = !it.isRepeatEnabled) }
            }
            is PlayerIntent.ProgressChanged -> {
                playbackManager.setProgress(intent.value)
            }
            is PlayerIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.send(PlayerEffect.NavigateBack)
                }
            }
            is PlayerIntent.SkipNext -> {
                playbackManager.skipNext()
            }
            is PlayerIntent.SkipPrevious -> {
                playbackManager.skipPrevious()
            }
        }
    }
}

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

/**
 * Now Playing (Çalar) ekranının MVI ViewModel sınıfı.
 *
 * SavedStateHandle aracılığıyla navigasyondan gelen "title", "subtitle", "startColor"
 * ve "endColor" argümanlarını okur ve başlangıç durumu olarak atar.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
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

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.TogglePlayPause -> {
                _uiState.update { it.copy(isPlaying = !it.isPlaying) }
            }
            is PlayerIntent.ToggleFavorite -> {
                _uiState.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is PlayerIntent.ToggleShuffle -> {
                _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
            }
            is PlayerIntent.ToggleRepeat -> {
                _uiState.update { it.copy(isRepeatEnabled = !it.isRepeatEnabled) }
            }
            is PlayerIntent.ProgressChanged -> {
                _uiState.update { current ->
                    // Yeni süreyi hesapla (örnek: 3:43 -> 223 saniye üzerinden)
                    val totalSeconds = 223
                    val currentSeconds = (totalSeconds * intent.value).toInt()
                    val minutes = currentSeconds / 60
                    val seconds = currentSeconds % 60
                    val timeString = String.format("%d:%02d", minutes, seconds)
                    current.copy(
                        progress = intent.value,
                        currentTime = timeString
                    )
                }
            }
            is PlayerIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.send(PlayerEffect.NavigateBack)
                }
            }
            is PlayerIntent.SkipNext -> {
                // Mock davranış: Şarkı durumunu veya bilgisini değiştirebiliriz.
            }
            is PlayerIntent.SkipPrevious -> {
                // Mock davranış.
            }
        }
    }
}

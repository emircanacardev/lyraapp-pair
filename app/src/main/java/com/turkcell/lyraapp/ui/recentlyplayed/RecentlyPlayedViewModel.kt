package com.turkcell.lyraapp.ui.recentlyplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
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
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentlyPlayedUiState())
    val uiState: StateFlow<RecentlyPlayedUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RecentlyPlayedEffect>(Channel.BUFFERED)
    val effect: Flow<RecentlyPlayedEffect> = _effect.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: RecentlyPlayedIntent) {
        when (intent) {
            is RecentlyPlayedIntent.Retry -> load()
            is RecentlyPlayedIntent.SongSelected -> viewModelScope.launch {
                _effect.send(
                    RecentlyPlayedEffect.NavigateToPlayer(
                        songId = intent.song.id,
                        title = intent.song.title,
                        artist = intent.song.artist,
                        startColor = intent.song.artworkStartColor,
                        endColor = intent.song.artworkEndColor,
                    )
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.getRecentlyPlayed(limit = 100)
                .onSuccess { songs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            songs = songs.map { dto ->
                                val (start, end) = artworkColorsFor(dto.id)
                                RecentlyPlayedSong(
                                    id = dto.id,
                                    title = dto.title,
                                    artist = dto.artist,
                                    artworkStartColor = start,
                                    artworkEndColor = end,
                                )
                            },
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Yüklenemedi.") }
                }
        }
    }

    companion object {
        private fun artworkColorsFor(id: String): Pair<Long, Long> {
            val hue = (abs(id.hashCode()) % 360).toFloat()
            val start = hslToArgb(hue, saturation = 0.50f, lightness = 0.55f)
            val end = hslToArgb(hue, saturation = 0.55f, lightness = 0.32f)
            return start to end
        }

        private fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Long {
            val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
            val hPrime = hue / 60f
            val x = c * (1f - kotlin.math.abs(hPrime % 2f - 1f))
            val (r1, g1, b1) = when {
                hPrime < 1f -> Triple(c, x, 0f)
                hPrime < 2f -> Triple(x, c, 0f)
                hPrime < 3f -> Triple(0f, c, x)
                hPrime < 4f -> Triple(0f, x, c)
                hPrime < 5f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val m = lightness - c / 2f
            val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}

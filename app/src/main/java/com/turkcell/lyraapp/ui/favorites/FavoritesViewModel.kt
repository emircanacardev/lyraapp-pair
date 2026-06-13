package com.turkcell.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
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
 * Favoriler (Beğenilen Şarkılar) ekranının MVI ViewModel'i.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FavoritesEffect>(Channel.BUFFERED)
    val effect: Flow<FavoritesEffect> = _effect.receiveAsFlow()

    init {
        loadLikedSongs()
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.PlayClicked -> {
                _uiState.update { current ->
                    val nextPlaying = !current.isPlaying
                    val nextPlayingSongId = if (nextPlaying && current.playingSongId == null && current.songs.isNotEmpty()) {
                        current.songs.first().id
                    } else {
                        current.playingSongId
                    }
                    current.copy(isPlaying = nextPlaying, playingSongId = nextPlayingSongId)
                }
            }
            is FavoritesIntent.ShuffleClicked -> {
                _uiState.update { current ->
                    val nextShuffled = !current.isShuffled
                    val nextSongs = if (nextShuffled) {
                        current.songs.shuffled()
                    } else {
                        current.songs.sortedBy { it.id }
                    }
                    current.copy(isShuffled = nextShuffled, songs = nextSongs)
                }
            }
            is FavoritesIntent.DownloadClicked -> {
                _uiState.update { current ->
                    current.copy(isDownloaded = !current.isDownloaded)
                }
            }
            is FavoritesIntent.SongClicked -> {
                _uiState.update { current ->
                    current.copy(playingSongId = intent.songId, isPlaying = true)
                }
            }
            is FavoritesIntent.FavoriteClicked -> {
                _uiState.update { current ->
                    val nextSongs = current.songs.filterNot { it.id == intent.songId }
                    val nextPlayingSongId = if (current.playingSongId == intent.songId) {
                        if (nextSongs.isNotEmpty()) nextSongs.first().id else null
                    } else {
                        current.playingSongId
                    }
                    val nextSubtitle = "${nextSongs.size} şarkı · ${nextSongs.size * 4} dk"
                    current.copy(
                        songs = nextSongs,
                        playingSongId = nextPlayingSongId,
                        subtitle = nextSubtitle
                    )
                }
            }
            is FavoritesIntent.Retry -> loadLikedSongs()
        }
    }

    private fun loadLikedSongs() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = favoritesRepository.getLikedSongs()
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { songs ->
                    _uiState.update { current ->
                        current.copy(
                            songs = songs,
                            subtitle = "${songs.size} şarkı · 19 dk"
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(FavoritesEffect.ShowError(error.message ?: "Beğenilen şarkılar yüklenemedi."))
                }
        }
    }
}

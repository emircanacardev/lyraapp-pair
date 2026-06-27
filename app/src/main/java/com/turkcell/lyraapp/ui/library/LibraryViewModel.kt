package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.PlaylistDto
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

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    init {
        loadPlaylists()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.FilterSelected -> _uiState.update { it.copy(selectedFilter = intent.filter) }
            is LibraryIntent.PlaylistClicked -> viewModelScope.launch { _effect.send(LibraryEffect.OpenPlaylist(intent.id)) }
            is LibraryIntent.DeletePlaylist -> deletePlaylist(intent.id)
            is LibraryIntent.SearchClicked -> viewModelScope.launch { _effect.send(LibraryEffect.NavigateToSearch) }
            is LibraryIntent.AddClicked -> viewModelScope.launch { _effect.send(LibraryEffect.NavigateToCreatePlaylist) }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.getUserPlaylists()
                .onSuccess { dtos ->
                    val apiItems = dtos.mapIndexed { index, dto -> dto.toPlaylistItem(index) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playlists = listOf(likedSongsItem()) + apiItems,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            authRepository.deletePlaylist(playlistId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(playlists = state.playlists.filter { it.id != playlistId })
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
}

private fun likedSongsItem() = PlaylistItem(
    id = "liked_songs",
    title = "Beğenilen Şarkılar",
    songCount = 0,
    isPinned = true,
    thumbnailColor = 0xFFE91E8CL,
    isLikedSongs = true,
)

private val thumbnailColors = listOf(
    0xFF7B5EA7L, 0xFF5B6AE8L, 0xFF26A69AL, 0xFF4DD0E1L, 0xFF00897BL,
    0xFFE57373L, 0xFFFF8A65L, 0xFFFFCA28L, 0xFF66BB6AL, 0xFF29B6F6L,
)

private fun PlaylistDto.toPlaylistItem(index: Int): PlaylistItem = PlaylistItem(
    id = id,
    title = name,
    songCount = 0,
    isPinned = false,
    thumbnailColor = thumbnailColors[index % thumbnailColors.size],
)

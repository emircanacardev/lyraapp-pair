package com.turkcell.lyraapp.ui.library.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
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
class CreatePlaylistViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val songsApi: SongsApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    init {
        loadSongs()
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged -> updateForm { it.copy(name = intent.value) }
            is CreatePlaylistIntent.DescriptionChanged -> _uiState.update { it.copy(description = intent.value) }
            is CreatePlaylistIntent.TogglePublic -> _uiState.update { it.copy(isPublic = !it.isPublic) }
            is CreatePlaylistIntent.ToggleSong -> toggleSong(intent.songId)
            is CreatePlaylistIntent.SaveClicked -> savePlaylist()
            is CreatePlaylistIntent.CloseClicked -> viewModelScope.launch { _effect.send(CreatePlaylistEffect.NavigateBack(playlistCreated = false)) }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSongsLoading = true) }
            runCatching { songsApi.getSongs(limit = 20) }
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            isSongsLoading = false,
                            availableSongs = page.data.mapIndexed { index, dto -> dto.toSelectable(index) },
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSongsLoading = false) }
                }
        }
    }

    private fun savePlaylist() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val description = state.description.takeIf { it.isNotBlank() }
            authRepository.createPlaylist(name = state.name, description = description)
                .onSuccess { playlist ->
                    state.selectedSongIds.forEach { songId ->
                        authRepository.addTrackToPlaylist(playlist.id, songId)
                    }
                    _effect.send(CreatePlaylistEffect.NavigateBack(playlistCreated = true))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun toggleSong(songId: String) {
        _uiState.update { state ->
            val updated = if (songId in state.selectedSongIds) {
                state.selectedSongIds - songId
            } else {
                state.selectedSongIds + songId
            }
            state.copy(selectedSongIds = updated)
        }
    }

    private fun updateForm(transform: (CreatePlaylistUiState) -> CreatePlaylistUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSaveEnabled = updated.name.isNotBlank())
        }
    }
}

private val songColors = listOf(
    0xFF2E8B57L, 0xFF8B5CF6L, 0xFF0EA5E9L, 0xFFE07B6AL, 0xFF7C3AE8L,
    0xFF10B981L, 0xFF14B8A6L, 0xFFEC4899L, 0xFF7B5EA7L, 0xFF26A69AL,
)

private fun SongDto.toSelectable(index: Int) = SelectableSong(
    id = id,
    title = title,
    artistName = artist,
    thumbnailColor = songColors[index % songColors.size],
)

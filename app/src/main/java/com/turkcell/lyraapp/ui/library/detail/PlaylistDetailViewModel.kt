package com.turkcell.lyraapp.ui.library.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.PlaylistWithSongsDto
import com.turkcell.lyraapp.data.songs.SongDto
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
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState(playlistId = playlistId))
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    init {
        if (playlistId == "liked_songs") {
            _uiState.update {
                it.copy(
                    title = "Beğenilen Şarkılar",
                    coverColor = 0xFFE91E8CL,
                    isLikedSongs = true,
                )
            }
        } else {
            loadPlaylist()
        }
    }

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.BackClicked -> viewModelScope.launch { _effect.send(PlaylistDetailEffect.NavigateBack) }
            is PlaylistDetailIntent.LikePlaylistClicked -> _uiState.update { it.copy(isLiked = !it.isLiked) }
            is PlaylistDetailIntent.LikeSongClicked -> toggleSongLike(intent.songId)
            is PlaylistDetailIntent.RemoveSong -> removeSong(intent.songId)
            is PlaylistDetailIntent.PlayClicked -> Unit
            is PlaylistDetailIntent.ShuffleClicked -> Unit
            is PlaylistDetailIntent.SongClicked -> Unit
        }
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.getPlaylistWithSongs(playlistId)
                .onSuccess { dto ->
                    _uiState.update { it.copy(isLoading = false).fromDto(dto) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun toggleSongLike(songId: String) {
        _uiState.update { state ->
            state.copy(
                songs = state.songs.map { song ->
                    if (song.id == songId) song.copy(isLiked = !song.isLiked) else song
                }
            )
        }
    }

    private fun removeSong(songId: String) {
        viewModelScope.launch {
            authRepository.removeTrackFromPlaylist(playlistId, songId)
                .onSuccess {
                    _uiState.update { state ->
                        val updated = state.songs.filter { it.id != songId }
                        state.copy(songs = updated, songCount = updated.size)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
}

private val songColors = listOf(
    0xFF7B5EA7L, 0xFF5B6AE8L, 0xFF26A69AL, 0xFF4DD0E1L, 0xFF00897BL,
    0xFFE57373L, 0xFF8B4513L, 0xFF2E8B57L, 0xFF0EA5E9L, 0xFF8B5CF6L,
)

private fun PlaylistDetailUiState.fromDto(dto: PlaylistWithSongsDto): PlaylistDetailUiState {
    val totalMs = dto.songs.sumOf { 0L }
    val totalMin = totalMs / 60000
    return copy(
        title = dto.name,
        description = dto.description ?: "",
        songCount = dto.songs.size,
        totalDuration = if (totalMin > 0) "$totalMin dk" else "",
        songs = dto.songs.mapIndexed { index, song -> song.toSongItem(index) },
    )
}

private fun SongDto.toSongItem(index: Int): SongItem = SongItem(
    id = id,
    title = title,
    artistName = artist,
    duration = "",
    thumbnailColor = songColors[index % songColors.size],
    isLiked = false,
)

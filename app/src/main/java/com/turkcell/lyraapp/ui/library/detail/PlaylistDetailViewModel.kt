package com.turkcell.lyraapp.ui.library.detail

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

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(staticDetail(playlistId))
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.BackClicked -> viewModelScope.launch { _effect.send(PlaylistDetailEffect.NavigateBack) }
            is PlaylistDetailIntent.LikePlaylistClicked -> _uiState.update { it.copy(isLiked = !it.isLiked) }
            is PlaylistDetailIntent.LikeSongClicked -> toggleSongLike(intent.songId)
            is PlaylistDetailIntent.PlayClicked -> Unit
            is PlaylistDetailIntent.ShuffleClicked -> Unit
            is PlaylistDetailIntent.SongClicked -> Unit
            is PlaylistDetailIntent.MoreSongClicked -> Unit
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
}

private fun staticDetail(playlistId: String): PlaylistDetailUiState = when (playlistId) {
    "1" -> PlaylistDetailUiState(
        playlistId = "1",
        title = "Beğenilen Şarkılar",
        songCount = 5,
        totalDuration = "19 dk",
        coverColor = 0xFFE91E8CL,
        isLikedSongs = true,
        songs = listOf(
            SongItem("ls1", "Gece Yarısı",   "Mavi Deniz",     "3:34", 0xFF2E8B57L, isLiked = true),
            SongItem("ls2", "Yıldız Tozu",   "Polaris",        "4:07", 0xFF0EA5E9L, isLiked = true),
            SongItem("ls3", "İlk Işık",      "Sabah Ezgisi",   "3:25", 0xFF10B981L, isLiked = true),
            SongItem("ls4", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFF8B4513L, isLiked = true, isPlaying = true),
            SongItem("ls5", "Derin Mavi",    "Okyanus",        "4:29", 0xFF1A5C3AL, isLiked = true),
        ),
    )
    "2" -> PlaylistDetailUiState(
        playlistId = "2",
        title = "Gece Sürüşü",
        description = "Karanlık yollar için synth-pop",
        ownerName = "Zeynep Kaya",
        songCount = 5,
        totalDuration = "23 dk",
        coverColor = 0xFF7B5EA7L,
        isLiked = false,
        songs = listOf(
            SongItem("s1", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFF8B4513L, isLiked = true,  isPlaying = true),
            SongItem("s2", "Gece Yarısı",   "Mavi Deniz",    "3:34", 0xFF2E8B57L, isLiked = true,  isPlaying = false),
            SongItem("s3", "Mor Bulutlar",  "Derin Kaya",    "3:52", 0xFF8B5CF6L, isLiked = false, isPlaying = false),
            SongItem("s4", "Son Tren",      "Peron",         "3:37", 0xFF14B8A6L, isLiked = false, isPlaying = false),
            SongItem("s5", "Yıldız Tozu",   "Polaris",       "4:07", 0xFF0EA5E9L, isLiked = false, isPlaying = false),
        ),
    )
    else -> PlaylistDetailUiState(playlistId = playlistId, title = "Çalma Listesi")
}

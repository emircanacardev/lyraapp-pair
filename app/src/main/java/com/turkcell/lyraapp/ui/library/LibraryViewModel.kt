package com.turkcell.lyraapp.ui.library

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
class LibraryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(playlists = staticPlaylists()))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.FilterSelected -> _uiState.update { it.copy(selectedFilter = intent.filter) }
            is LibraryIntent.PlaylistClicked -> viewModelScope.launch { _effect.send(LibraryEffect.OpenPlaylist(intent.id)) }
            is LibraryIntent.MoreClicked -> Unit
            is LibraryIntent.SearchClicked -> viewModelScope.launch { _effect.send(LibraryEffect.NavigateToSearch) }
            is LibraryIntent.AddClicked -> viewModelScope.launch { _effect.send(LibraryEffect.NavigateToCreatePlaylist) }
        }
    }
}

private fun staticPlaylists(): List<PlaylistItem> = listOf(
    PlaylistItem(id = "1", title = "Beğenilen Şarkılar", songCount = 5, isPinned = true,  thumbnailColor = 0xFFE91E8CL, isLikedSongs = true),
    PlaylistItem(id = "2", title = "Gece Sürüşü",        songCount = 6, isPinned = false, thumbnailColor = 0xFF7B5EA7L),
    PlaylistItem(id = "3", title = "Sabah Kahvesi",       songCount = 5, isPinned = false, thumbnailColor = 0xFF5B6AE8L),
    PlaylistItem(id = "4", title = "Odaklan",             songCount = 5, isPinned = false, thumbnailColor = 0xFF26A69AL),
    PlaylistItem(id = "5", title = "Yaz Anıları",         songCount = 5, isPinned = false, thumbnailColor = 0xFF4DD0E1L),
    PlaylistItem(id = "6", title = "Akustik Akşam",       songCount = 4, isPinned = false, thumbnailColor = 0xFF00897BL),
)

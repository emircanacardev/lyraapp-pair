package com.turkcell.lyraapp.ui.library.create

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
class CreatePlaylistViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreatePlaylistUiState(availableSongs = staticSongs())
    )
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged -> updateForm { it.copy(name = intent.value) }
            is CreatePlaylistIntent.DescriptionChanged -> _uiState.update { it.copy(description = intent.value) }
            is CreatePlaylistIntent.TogglePublic -> _uiState.update { it.copy(isPublic = !it.isPublic) }
            is CreatePlaylistIntent.ToggleSong -> toggleSong(intent.songId)
            is CreatePlaylistIntent.SaveClicked -> viewModelScope.launch { _effect.send(CreatePlaylistEffect.NavigateBack) }
            is CreatePlaylistIntent.CloseClicked -> viewModelScope.launch { _effect.send(CreatePlaylistEffect.NavigateBack) }
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

private fun staticSongs(): List<SelectableSong> = listOf(
    SelectableSong("cs1", "Gece Yarısı",  "Mavi Deniz",    0xFF2E8B57L),
    SelectableSong("cs2", "Sessiz Şehir", "Ela Tuna",      0xFF8B5CF6L),
    SelectableSong("cs3", "Yıldız Tozu",  "Polaris",       0xFF0EA5E9L),
    SelectableSong("cs4", "Sahil Yolu",   "Kumsal",        0xFFE07B6AL),
    SelectableSong("cs5", "Mor Bulutlar", "Derin Kaya",    0xFF7C3AE8L),
    SelectableSong("cs6", "İlk Işık",     "Sabah Ezgisi",  0xFF10B981L),
    SelectableSong("cs7", "Kayıp Anlar",  "Eko",           0xFF14B8A6L),
)

package com.turkcell.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.search.Genre
import com.turkcell.lyraapp.data.search.SearchRepository
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
 * Arama ekranının ViewModel'i. MVI prensiplerine göre yazılmıştır.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    init {
        loadGenres()
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                _uiState.update { current ->
                    val updated = current.copy(query = intent.value)
                    updated.copy(filteredGenres = filterGenres(updated.genres, updated.query, updated.selectedFilter))
                }
            }
            is SearchIntent.FilterSelected -> {
                _uiState.update { current ->
                    val updated = current.copy(selectedFilter = intent.filter)
                    updated.copy(filteredGenres = filterGenres(updated.genres, updated.query, updated.selectedFilter))
                }
            }
            is SearchIntent.Retry -> loadGenres()
        }
    }

    private fun loadGenres() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = searchRepository.getGenres()
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { genres ->
                    _uiState.update { current ->
                        current.copy(
                            genres = genres,
                            filteredGenres = filterGenres(genres, current.query, current.selectedFilter)
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(SearchEffect.ShowError(error.message ?: "Müzik türleri yüklenemedi."))
                }
        }
    }

    private fun filterGenres(genres: List<Genre>, query: String, filter: String): List<Genre> {
        return genres.filter { genre ->
            val matchesFilter = if (filter == "Hepsi") {
                true
            } else {
                genre.name.equals(filter, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                genre.name.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }
}

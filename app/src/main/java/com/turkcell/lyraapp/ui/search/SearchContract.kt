package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.search.Genre

/**
 * Arama ekranının MVI sözleşmesi: State (durum), Intent (niyet) ve Effect (olay).
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val selectedFilter: String = "Hepsi",
    val availableFilters: List<String> = listOf("Hepsi", "Pop", "Elektronik", "Akustik"),
    val genres: List<Genre> = emptyList(),
    val filteredGenres: List<Genre> = emptyList()
)

sealed interface SearchIntent {
    data class QueryChanged(val value: String) : SearchIntent
    data class FilterSelected(val filter: String) : SearchIntent
    data object Retry : SearchIntent
}

sealed interface SearchEffect {
    data class ShowError(val message: String) : SearchEffect
}

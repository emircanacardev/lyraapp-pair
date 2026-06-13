package com.turkcell.lyraapp.data.search

/**
 * Arama ve kategori verilerinin veri kaynağı soyutlaması.
 */
interface SearchRepository {
    /** Tüm müzik türü kategorilerini döndürür. */
    suspend fun getGenres(): Result<List<Genre>>
}

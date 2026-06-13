package com.turkcell.lyraapp.data.favorites

/**
 * Beğenilen Şarkılar verilerinin veri kaynağı soyutlaması.
 */
interface FavoritesRepository {
    /** Beğenilen şarkı listesini döndürür. */
    suspend fun getLikedSongs(): Result<List<Song>>
}

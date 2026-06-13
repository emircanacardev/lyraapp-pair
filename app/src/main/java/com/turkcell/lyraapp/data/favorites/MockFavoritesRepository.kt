package com.turkcell.lyraapp.data.favorites

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [FavoritesRepository]'nin MOCK (statik veri) implementasyonu.
 *
 * Tasarımdaki beğenilen 5 şarkıyı süre ve gradyan renk bilgileriyle döner.
 */
class MockFavoritesRepository @Inject constructor() : FavoritesRepository {

    override suspend fun getLikedSongs(): Result<List<Song>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(LIKED_SONGS)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 400L

        val LIKED_SONGS = listOf(
            Song("s-1", "Gece Yarısı", "Mavi Deniz", "3:34", 0xFF3FAE9C, 0xFF356B2A, hasPattern = true),
            Song("s-2", "Yıldız Tozu", "Polaris", "4:07", 0xFF2A5F73, 0xFF467B92, hasPattern = true),
            Song("s-3", "İlk Işık", "Sabah Ezgisi", "3:25", 0xFF3D5A80, 0xFF1B2A45, hasPattern = true),
            Song("s-4", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFFD98E4A, 0xFF8A5526, hasPattern = false), // Bu çalan şarkı olacak
            Song("s-5", "Derin Mavi", "Okyanus", "4:29", 0xFF6FBF5A, 0xFF356B2A, hasPattern = true)
        )
    }
}

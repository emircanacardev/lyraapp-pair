package com.turkcell.lyraapp.data.search

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [SearchRepository]'nin MOCK (statik veri) implementasyonu.
 *
 * Tasarımdaki müzik türlerini ve bunlara ait renk/desen bilgilerini statik olarak döndürür.
 * `delay(...)` ile ağ gecikmesini simüle eder.
 */
class MockSearchRepository @Inject constructor() : SearchRepository {

    override suspend fun getGenres(): Result<List<Genre>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(GENRES)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L

        val GENRES = listOf(
            Genre("g-1", "Pop", 0xFF3FAE9C, 0xFF8AD1C2, PatternType.NONE),
            Genre("g-2", "Elektronik", 0xFF8B6FB8, 0xFFB19CD9, PatternType.NONE),
            Genre("g-3", "Akustik", 0xFF6A5FB8, 0xFF8A7ED1, PatternType.CIRCLES),
            Genre("g-4", "Lo-fi", 0xFF2A5F73, 0xFF467B92, PatternType.CURVES),
            Genre("g-5", "Indie", 0xFF4A3D6B, 0xFF66588B, PatternType.CIRCLES),
            Genre("g-6", "Jazz", 0xFF356B2A, 0xFF548B47, PatternType.CURVES),
            Genre("g-7", "Klasik", 0xFF7B2949, 0xFF9E4767, PatternType.CIRCLES),
            Genre("g-8", "Yolculuk", 0xFFD98E4A, 0xFFF0AD72, PatternType.NONE),
        )
    }
}

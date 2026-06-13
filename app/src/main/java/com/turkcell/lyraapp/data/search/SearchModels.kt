package com.turkcell.lyraapp.data.search

/**
 * Arama ekranında gösterilecek her bir müzik türü (kategori) kartının modeli.
 */
data class Genre(
    val id: String,
    val name: String,
    val startColor: Long,
    val endColor: Long,
    val patternType: PatternType
)

enum class PatternType {
    NONE,
    CIRCLES,
    CURVES
}

package com.turkcell.lyraapp.data.favorites

/**
 * Favoriler ekranındaki her bir şarkı kaydının veri modeli.
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val startColor: Long,
    val endColor: Long,
    val hasPattern: Boolean = true
)

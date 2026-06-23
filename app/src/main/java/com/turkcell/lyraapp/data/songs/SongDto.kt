package com.turkcell.lyraapp.data.songs

import kotlinx.serialization.Serializable

@Serializable
data class SongsPageDto(
    val data: List<SongDto> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
)

@Serializable
data class StreamUrlEnvelope(
    val data: StreamUrlDto,
)

@Serializable
data class StreamUrlDto(
    val url: String,
    val expiresAt: String? = null,
    val mimeType: String? = null,
)

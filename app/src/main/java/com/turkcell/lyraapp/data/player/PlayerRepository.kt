package com.turkcell.lyraapp.data.player

interface PlayerRepository {
    suspend fun getStreamUrl(songId: String): Result<String>
}

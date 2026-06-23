package com.turkcell.lyraapp.data.player

import com.turkcell.lyraapp.data.songs.SongsApi
import javax.inject.Inject

class DefaultPlayerRepository @Inject constructor(
    private val songsApi: SongsApi,
) : PlayerRepository {

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }
}

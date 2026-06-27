package com.turkcell.lyraapp.data.player

import com.turkcell.lyraapp.data.auth.AuthApi
import com.turkcell.lyraapp.data.auth.PlaybackNextRequest
import javax.inject.Inject

class DefaultPlayerRepository @Inject constructor(
    private val authApi: AuthApi,
) : PlayerRepository {

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        authApi.playbackNext(PlaybackNextRequest(songId)).data.stream.url
    }
}

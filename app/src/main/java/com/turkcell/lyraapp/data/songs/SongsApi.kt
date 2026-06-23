package com.turkcell.lyraapp.data.songs

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SongsApi {

    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("q") query: String? = null,
    ): SongsPageDto

    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(@Path("id") id: String): StreamUrlEnvelope
}

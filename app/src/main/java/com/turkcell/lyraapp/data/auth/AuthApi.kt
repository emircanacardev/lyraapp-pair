package com.turkcell.lyraapp.data.auth

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(
        @Body request: OtpRequest
    ): OtpResponseEnvelope

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(
        @Body request: OtpVerifyRequest
    ): AuthSessionEnvelope

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ): AuthTokensEnvelope

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): LogoutResponseEnvelope

    @POST("api/v1/me/update-informations")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): ProfileUpdateResponseEnvelope

    @GET("api/v1/me")
    suspend fun getCurrentUser(): UserResponseEnvelope

    @POST("api/v1/me/plays")
    suspend fun recordPlay(@Body request: RecordPlayRequest): RecordPlayResponseEnvelope

    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(@Query("limit") limit: Int? = null): SongListEnvelope

    @GET("api/v1/me/for-you")
    suspend fun getForYou(@Query("limit") limit: Int? = null): SongListEnvelope

    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(@Query("limit") limit: Int? = null): SongListEnvelope

    @GET("api/v1/me/playlists")
    suspend fun getUserPlaylists(): PlaylistListEnvelope

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): PlaylistEnvelope

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @Path("id") playlistId: String,
        @Body request: AddTrackRequest
    ): AddTrackResponseEnvelope

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrackFromPlaylist(
        @Path("id") playlistId: String,
        @Path("songId") songId: String
    ): RemoveTrackResponseEnvelope

    @DELETE("api/v1/me/playlists/{id}")
    suspend fun deletePlaylist(
        @Path("id") playlistId: String
    ): DeletePlaylistResponseEnvelope

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistWithSongs(
        @Path("id") playlistId: String
    ): PlaylistWithSongsEnvelope

    @POST("api/v1/me/playback/next")
    suspend fun playbackNext(@Body request: PlaybackNextRequest): PlaybackNextResponseEnvelope
}

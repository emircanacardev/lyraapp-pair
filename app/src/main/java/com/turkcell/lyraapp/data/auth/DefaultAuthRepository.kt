package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.songs.SongDto
import javax.inject.Inject

class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
) : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpResponseDto> = runCatching {
        authApi.requestOtp(OtpRequest(phone)).data
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthSessionDto> = runCatching {
        authApi.verifyOtp(OtpVerifyRequest(phone, code)).data
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokensDto> = runCatching {
        authApi.refreshToken(TokenRefreshRequest(refreshToken)).data
    }

    override suspend fun logout(refreshToken: String): Result<Boolean> = runCatching {
        authApi.logout(LogoutRequest(refreshToken)).data.revoked
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<UserDto> = runCatching {
        authApi.updateProfile(ProfileUpdateRequest(firstName, lastName, birthDate)).data
    }

    override suspend fun getCurrentUser(): Result<UserDto> = runCatching {
        authApi.getCurrentUser().data
    }

    override suspend fun recordPlay(songId: String): Result<Boolean> = runCatching {
        authApi.recordPlay(RecordPlayRequest(songId)).data.recorded
    }

    override suspend fun getRecentlyPlayed(limit: Int?): Result<List<SongDto>> = runCatching {
        authApi.getRecentlyPlayed(limit).data
    }

    override suspend fun getForYou(limit: Int?): Result<List<SongDto>> = runCatching {
        authApi.getForYou(limit).data
    }

    override suspend fun getRecommendations(limit: Int?): Result<List<SongDto>> = runCatching {
        authApi.getRecommendations(limit).data
    }

    override suspend fun getUserPlaylists(): Result<List<PlaylistDto>> = runCatching {
        authApi.getUserPlaylists().data
    }

    override suspend fun createPlaylist(name: String, description: String?): Result<PlaylistDto> = runCatching {
        authApi.createPlaylist(CreatePlaylistRequest(name, description)).data
    }

    override suspend fun addTrackToPlaylist(playlistId: String, songId: String): Result<Boolean> = runCatching {
        authApi.addTrackToPlaylist(playlistId, AddTrackRequest(songId)).data.added
    }

    override suspend fun removeTrackFromPlaylist(playlistId: String, songId: String): Result<Boolean> = runCatching {
        authApi.removeTrackFromPlaylist(playlistId, songId).data.removed
    }

    @Deprecated("Yeni OTP tabanlı giriş akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Geleneksel login metodu artık desteklenmemektedir. OTP akışını kullanın."))
    }

    @Deprecated("Yeni OTP tabanlı kayıt akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Geleneksel register metodu artık desteklenmemektedir. OTP akışını kullanın."))
    }
}

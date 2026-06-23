package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [AuthRepository]'nin sahte (stub) implementasyonu.
 *
 * Gerçek bir ağ çağrısı yapmaz; yalnızca MVI akışının uçtan uca çalışmasını sağlamak için
 * bir gecikme ile ağ davranışını taklit eder. Gerçek API geldiğinde bu sınıf bir
 * ağ tabanlı implementasyonla değiştirilir.
 */
class FakeAuthRepository @Inject constructor() : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpResponseDto> {
        delay(NETWORK_DELAY_MS)
        return Result.success(OtpResponseDto(sent = true, firstTime = false))
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthSessionDto> {
        delay(NETWORK_DELAY_MS)
        if (code in listOf("280600", "260702", "250506", "101000", "346134", "123456")) {
            val user = UserDto(
                id = "7c3a6b54-9b2e-4a1d-bf0a-9f0b2c1d3e4f",
                phone = phone,
                displayName = null,
                firstName = "Halit",
                lastName = "Kalaycı",
                birthDate = "1995-06-20",
                createdAt = "2026-06-23T20:00:00Z",
                profileCompleted = true
            )
            return Result.success(
                AuthSessionDto(
                    accessToken = "fake_access_token",
                    refreshToken = "fake_refresh_token",
                    tokenType = "Bearer",
                    expiresIn = 900,
                    user = user,
                    firstTime = false
                )
            )
        }
        return Result.failure(Exception("Geçersiz doğrulama kodu."))
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokensDto> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            AuthTokensDto(
                accessToken = "new_fake_access_token",
                refreshToken = "new_fake_refresh_token",
                tokenType = "Bearer",
                expiresIn = 900
            )
        )
    }

    override suspend fun logout(refreshToken: String): Result<Boolean> {
        delay(NETWORK_DELAY_MS)
        return Result.success(true)
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<UserDto> {
        delay(NETWORK_DELAY_MS)
        val user = UserDto(
            id = "7c3a6b54-9b2e-4a1d-bf0a-9f0b2c1d3e4f",
            phone = "+905551112233",
            displayName = null,
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            createdAt = "2026-06-23T20:00:00Z",
            profileCompleted = true
        )
        return Result.success(user)
    }

    override suspend fun getCurrentUser(): Result<UserDto> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            UserDto(
                id = "7c3a6b54-9b2e-4a1d-bf0a-9f0b2c1d3e4f",
                phone = "+905551112233",
                displayName = null,
                firstName = "Halit",
                lastName = "Kalaycı",
                birthDate = "1995-06-20",
                createdAt = "2026-06-23T20:00:00Z",
                profileCompleted = true
            )
        )
    }

    override suspend fun recordPlay(songId: String): Result<Boolean> {
        delay(NETWORK_DELAY_MS)
        return Result.success(true)
    }

    override suspend fun getRecentlyPlayed(limit: Int?): Result<List<SongDto>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(emptyList())
    }

    override suspend fun getForYou(limit: Int?): Result<List<SongDto>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(emptyList())
    }

    override suspend fun getRecommendations(limit: Int?): Result<List<SongDto>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(emptyList())
    }

    override suspend fun getUserPlaylists(): Result<List<PlaylistDto>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(emptyList())
    }

    override suspend fun createPlaylist(name: String, description: String?): Result<PlaylistDto> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            PlaylistDto(
                id = "p_fake",
                name = name,
                description = description,
                createdAt = "2026-06-23T20:00:00Z",
                ownerId = "7c3a6b54-9b2e-4a1d-bf0a-9f0b2c1d3e4f"
            )
        )
    }

    override suspend fun addTrackToPlaylist(playlistId: String, songId: String): Result<Boolean> {
        delay(NETWORK_DELAY_MS)
        return Result.success(true)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: String, songId: String): Result<Boolean> {
        delay(NETWORK_DELAY_MS)
        return Result.success(true)
    }

    @Deprecated("Yeni OTP tabanlı giriş akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Şifre boş olamaz."))
        }
    }

    @Deprecated("Yeni OTP tabanlı kayıt akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (firstName.isNotBlank() && lastName.isNotBlank() && password.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Hesap bilgileri eksik."))
        }
    }

    private companion object {
        const val NETWORK_DELAY_MS = 1_000L
    }
}

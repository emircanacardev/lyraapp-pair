package com.turkcell.lyraapp.data.auth

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

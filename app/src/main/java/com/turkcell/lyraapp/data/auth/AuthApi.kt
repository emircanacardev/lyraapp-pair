package com.turkcell.lyraapp.data.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
}

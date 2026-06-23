package com.turkcell.lyraapp.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    val phone: String
)

@Serializable
data class OtpResponseEnvelope(
    val data: OtpResponseDto
)

@Serializable
data class OtpResponseDto(
    val sent: Boolean,
    val firstTime: Boolean
)

@Serializable
data class OtpVerifyRequest(
    val phone: String,
    val code: String
)

@Serializable
data class AuthSessionEnvelope(
    val data: AuthSessionDto
)

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: UserDto,
    val firstTime: Boolean
)

@Serializable
data class UserDto(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val createdAt: String,
    val profileCompleted: Boolean
)

@Serializable
data class TokenRefreshRequest(
    val refreshToken: String
)

@Serializable
data class AuthTokensEnvelope(
    val data: AuthTokensDto
)

@Serializable
data class AuthTokensDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class LogoutResponseEnvelope(
    val data: LogoutResponseDto
)

@Serializable
data class LogoutResponseDto(
    val revoked: Boolean
)

@Serializable
data class ProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

@Serializable
data class ProfileUpdateResponseEnvelope(
    val data: UserDto
)

@Serializable
data class UserResponseEnvelope(
    val data: UserDto
)



package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.songs.SongDto
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

@Serializable
data class RecordPlayRequest(
    val songId: String
)

@Serializable
data class RecordPlayDto(
    val recorded: Boolean
)

@Serializable
data class RecordPlayResponseEnvelope(
    val data: RecordPlayDto
)

@Serializable
data class SongListEnvelope(
    val data: List<SongDto>
)

@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val ownerId: String? = null
)

@Serializable
data class PlaylistEnvelope(
    val data: PlaylistDto
)

@Serializable
data class PlaylistListEnvelope(
    val data: List<PlaylistDto>
)

@Serializable
data class CreatePlaylistRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class AddTrackRequest(
    val songId: String
)

@Serializable
data class AddTrackDto(
    val added: Boolean
)

@Serializable
data class AddTrackResponseEnvelope(
    val data: AddTrackDto
)

@Serializable
data class RemoveTrackDto(
    val removed: Boolean
)

@Serializable
data class RemoveTrackResponseEnvelope(
    val data: RemoveTrackDto
)

@Serializable
data class DeletePlaylistDto(
    val deleted: Boolean
)

@Serializable
data class DeletePlaylistResponseEnvelope(
    val data: DeletePlaylistDto
)

@Serializable
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val ownerId: String? = null,
    val songs: List<com.turkcell.lyraapp.data.songs.SongDto> = emptyList(),
)

@Serializable
data class PlaylistWithSongsEnvelope(
    val data: PlaylistWithSongsDto
)


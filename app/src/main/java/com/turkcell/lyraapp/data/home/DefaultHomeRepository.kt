package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.PlaylistDto
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class DefaultHomeRepository @Inject constructor(
    private val songsApi: SongsApi,
    private val authRepository: AuthRepository,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        coroutineScope {
            val songsDeferred           = async { songsApi.getSongs(limit = SONGS_PAGE_SIZE).data }
            val playlistsDeferred       = async { authRepository.getUserPlaylists() }
            val recentlyPlayedDeferred  = async { authRepository.getRecentlyPlayed(limit = 10) }
            val recommendationsDeferred = async { authRepository.getRecommendations(limit = 6) }
            val forYouDeferred          = async { authRepository.getForYou(limit = 20) }
            val userDeferred            = async { authRepository.getCurrentUser() }

            val songs          = songsDeferred.await().map { it.toHomeSong() }
            val playlists      = playlistsDeferred.await()
                .getOrDefault(emptyList()).map { it.toFeaturedPlaylist() }
            val recentlyPlayed = recentlyPlayedDeferred.await()
                .getOrDefault(emptyList()).map { it.toRecentlyPlayed() }
            val recommendations = recommendationsDeferred.await()
                .getOrDefault(emptyList()).map { it.toRecommendation() }
            val forYou         = forYouDeferred.await()
                .getOrDefault(emptyList()).map { it.toForYouSong() }
            val user           = userDeferred.await().getOrNull()

            HomeFeed(
                userInitials    = buildInitials(user?.firstName, user?.lastName),
                songs           = songs,
                playlists       = playlists,
                recommendations = recommendations,
                recentlyPlayed  = recentlyPlayed,
                forYouSongs     = forYou,
            )
        }
    }

    override suspend fun getRecentlyPlayed(): Result<List<RecentlyPlayed>> = runCatching {
        authRepository.getRecentlyPlayed(limit = 10)
            .getOrDefault(emptyList())
            .map { it.toRecentlyPlayed() }
    }

    private fun PlaylistDto.toFeaturedPlaylist(): FeaturedPlaylist {
        val (start, end) = artworkColorsFor(id)
        return FeaturedPlaylist(id = id, name = name, description = description, artworkStartColor = start, artworkEndColor = end)
    }

    private fun SongDto.toHomeSong(): HomeSong {
        val (start, end) = artworkColorsFor(id)
        return HomeSong(id = id, title = title, artist = artist, artworkStartColor = start, artworkEndColor = end)
    }

    private fun SongDto.toRecentlyPlayed(): RecentlyPlayed {
        val (start, end) = artworkColorsFor(id)
        return RecentlyPlayed(id = id, title = title, subtitle = artist, artworkStartColor = start, artworkEndColor = end)
    }

    private fun SongDto.toRecommendation(): Recommendation {
        val (start, end) = artworkColorsFor(id)
        return Recommendation(id = id, title = title, artist = artist, artworkStartColor = start, artworkEndColor = end)
    }

    private fun SongDto.toForYouSong(): ForYouSong {
        val (start, end) = artworkColorsFor(id)
        return ForYouSong(id = id, title = title, artist = artist, artworkStartColor = start, artworkEndColor = end)
    }

    private fun buildInitials(firstName: String?, lastName: String?): String {
        val first = firstName?.firstOrNull()?.uppercaseChar()
        val last  = lastName?.firstOrNull()?.uppercaseChar()
        return listOfNotNull(first, last).joinToString("").ifBlank { "?" }
    }

    private companion object {
        const val SONGS_PAGE_SIZE = 20

        fun artworkColorsFor(id: String): Pair<Long, Long> {
            val hue   = (abs(id.hashCode()) % 360).toFloat()
            val start = hslToArgb(hue, saturation = 0.50f, lightness = 0.55f)
            val end   = hslToArgb(hue, saturation = 0.55f, lightness = 0.32f)
            return start to end
        }

        fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Long {
            val c      = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
            val hPrime = hue / 60f
            val x      = c * (1f - kotlin.math.abs(hPrime % 2f - 1f))
            val (r1, g1, b1) = when {
                hPrime < 1f -> Triple(c, x, 0f)
                hPrime < 2f -> Triple(x, c, 0f)
                hPrime < 3f -> Triple(0f, c, x)
                hPrime < 4f -> Triple(0f, x, c)
                hPrime < 5f -> Triple(x, 0f, c)
                else        -> Triple(c, 0f, x)
            }
            val m = lightness - c / 2f
            val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}

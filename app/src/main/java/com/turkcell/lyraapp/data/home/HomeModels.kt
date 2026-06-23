package com.turkcell.lyraapp.data.home

data class HomeSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class FeaturedPlaylist(
    val id: String,
    val name: String,
    val description: String?,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class HomeFeed(
    val userInitials: String,
    val songs: List<HomeSong>,
    val playlists: List<FeaturedPlaylist>,
    val recommendations: List<Recommendation>,
    val recentlyPlayed: List<RecentlyPlayed>,
    val forYouSongs: List<ForYouSong>,
)

/** "Öneriler" grid'indeki öneri şarkısı. */
data class Recommendation(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** "Son çalınanlar" bölümündeki öğe; [subtitle] sanatçı/albüm bilgisini taşır. */
data class RecentlyPlayed(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** "Senin İçin" bölümündeki şarkı öğesi. */
data class ForYouSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

package com.turkcell.lyraapp.data.home

data class HomeSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class HomeFeed(
    val userInitials: String,
    val songs: List<HomeSong>,
    val quickPicks: List<QuickPick>,
    val recentlyPlayed: List<RecentlyPlayed>,
    val playlistsForYou: List<PlaylistForYou>,
)

/** "Ne dinlemek istersin?" grid'indeki hızlı seçim öğesi. */
data class QuickPick(
    val id: String,
    val title: String,
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

/** "Senin için çalma listeleri" bölümündeki öğe. */
data class PlaylistForYou(
    val id: String,
    val title: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

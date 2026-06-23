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
    val forYouSongs: List<ForYouSong>,
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

/** "Senin İçin" bölümündeki şarkı öğesi. */
data class ForYouSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

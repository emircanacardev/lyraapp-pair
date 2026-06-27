package com.turkcell.lyraapp.ui.navigation

/**
 * Uygulamadaki navigasyon hedeflerinin tek doğruluk kaynağı.
 *
 * Her hedef benzersiz bir [route] string'iyle temsil edilir; [LyraNavHost] bu route'lar
 * üzerinden composable'ları bağlar. Yeni bir ekran eklendiğinde buraya bir hedef eklenir.
 */
enum class LyraDestination(val route: String) {
    Login("login"),
    Register("register"),
    ProfileComplete("profile_complete"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),
    PlaylistDetail("library/detail/{playlistId}"),
    CreatePlaylist("library/create"),
    Player("player?songId={songId}&title={title}&artist={artist}&startColor={startColor}&endColor={endColor}"),
    AllRecentlyPlayed("recently-played"),
    Premium("premium"),
    Payment("premium/payment?planId={planId}&planType={planType}&planName={planName}&planPrice={planPrice}"),
    PaymentSuccess("premium/success?planType={planType}"),
}

fun paymentRoute(planId: String, planType: String, planName: String, planPrice: Int) =
    "premium/payment?planId=$planId&planType=$planType&planName=${planName.encodeForRoute()}&planPrice=$planPrice"

private fun String.encodeForRoute() = java.net.URLEncoder.encode(this, "UTF-8")

fun paymentSuccessRoute(planType: String) = "premium/success?planType=$planType"

fun playlistDetailRoute(playlistId: String) = "library/detail/$playlistId"

package com.turkcell.lyraapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.library.create.CreatePlaylistRoute
import com.turkcell.lyraapp.ui.library.detail.PlaylistDetailRoute
import com.turkcell.lyraapp.ui.search.SearchRoute
import com.turkcell.lyraapp.ui.favorites.FavoritesRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.player.PlayerRoute
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.data.player.PlayingTrack
import com.turkcell.lyraapp.data.player.PlaybackManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Uygulamanın iskelet navigasyon yapısı.
 *
 * Tek [NavHost], Auth grafiği ile ana akış sekmelerini barındırır; başlangıç hedefi
 * [LyraDestination.Login]'dir. Dış [Scaffold]'ın `bottomBar` yuvasındaki [LyraBottomBar]
 * yalnızca üst düzey sekme rotalarında görünür; böylece çubuk her ana sayfanın altında
 * yer alır, Auth ekranlarında gizlenir.
 *
 * Her ekranın `Route` composable'ı, MVI Effect'lerini buradan sağlanan navigasyon
 * lambda'larına köprüler (ViewModel navigasyon API'si bilmez; bkz. mvi-viewmodel-rules §6).
 *
 * Dış Scaffold'ın `contentWindowInsets`'i sıfırlanır: sistem çubuğu boşluklarını her ekran
 * kendisi yönetir (Login/Register'da olduğu gibi); içerik dolgusu yalnızca alt çubuğun
 * yüksekliğini taşır.
 */
@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current.applicationContext
    val playbackManager = remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            PlaybackEntryPoint::class.java
        ).playbackManager()
    }
    val playingTrack by playbackManager.playingTrack.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                val track = playingTrack
                if (track != null && isTopLevelRoute(currentRoute)) {
                    MiniPlayer(
                        track = track,
                        onPlayPauseToggle = { playbackManager.togglePlayPause() },
                        onFavoriteToggle = { playbackManager.toggleFavorite() },
                        onSkipNext = { playbackManager.skipNext() },
                        onClick = {
                            navController.navigate("player?title=${track.title}&subtitle=${track.subtitle}&startColor=${track.startColor}&endColor=${track.endColor}")
                        }
                    )
                }
                if (isTopLevelRoute(currentRoute)) {
                    LyraBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = navController::navigateToTab,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LyraDestination.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Login.route) {
                LoginRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToRegister = {
                        navController.navigate(LyraDestination.Register.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.Register.route) {
                RegisterRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onNavigateToPlayer = { title, subtitle, startColor, endColor ->
                        navController.navigate("player?title=$title&subtitle=$subtitle&startColor=$startColor&endColor=$endColor")
                    }
                )
            }
            composable(LyraDestination.Search.route) { SearchRoute() }
            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onOpenPlaylist = { id -> navController.navigate(playlistDetailRoute(id)) },
                    onCreatePlaylist = { navController.navigate(LyraDestination.CreatePlaylist.route) },
                )
            }
            composable(LyraDestination.Favorites.route) {
                FavoritesRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(LyraDestination.Profile.route) { ProfileRoute() }
            composable(
                route = LyraDestination.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) {
                PlaylistDetailRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(LyraDestination.CreatePlaylist.route) {
                CreatePlaylistRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = LyraDestination.Player.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType; nullable = true },
                    navArgument("subtitle") { type = NavType.StringType; nullable = true },
                    navArgument("startColor") { type = NavType.LongType; defaultValue = 0xFF8B6FB8L },
                    navArgument("endColor") { type = NavType.LongType; defaultValue = 0xFF4A3D6BL },
                ),
            ) {
                PlayerRoute(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Alt çubuk sekmesine standart desenle geçiş yapar: back stack'te sekme kopyası birikmez
 * (`launchSingleTop`), sekmeler arası geçişte durum saklanır/geri yüklenir
 * (`saveState`/`restoreState`) ve geri tuşu daima Home'a döner (`popUpTo(Home)`).
 */
private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Auth akışını back stack'ten temizleyerek Home'a geçer (geri tuşu Login'e dönmez). */
private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Geçici sekme içeriği. Sekme ekranları henüz kapsamda değildir; her biri kendi
 * feature paketinde MVI sözleşmesiyle (Contract + ViewModel + Route/Screen) yazıldığında
 * bu composable kaldırılacak ve rotalar gerçek Route'lara bağlanacaktır.
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlaybackEntryPoint {
    fun playbackManager(): PlaybackManager
}

@Composable
fun MiniPlayer(
    track: PlayingTrack,
    onPlayPauseToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(track.startColor), Color(track.endColor))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LyraIcons.Waveform,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = track.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (track.isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                        contentDescription = "Beğen",
                        tint = if (track.isFavorite) Color(0xFFF5B2C3) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onPlayPauseToggle) {
                    Icon(
                        imageVector = if (track.isPlaying) MiniPlayerIcons.Pause else MiniPlayerIcons.PlayArrow,
                        contentDescription = if (track.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = MiniPlayerIcons.SkipNext,
                        contentDescription = "Sonraki",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(track.progress)
                        .fillMaxHeight()
                        .background(Color(0xFFF5B2C3))
                )
            }
        }
    }
}

private object MiniPlayerIcons {
    val PlayArrow: ImageVector by lazy {
        lyraIcon("PlayArrow", "M8 5v14l11-7z")
    }

    val Pause: ImageVector by lazy {
        lyraIcon("Pause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z")
    }

    val SkipNext: ImageVector by lazy {
        lyraIcon("SkipNext", "M6 18l8.5-6L6 6v12zM16 6v12h2V6z")
    }

    private fun lyraIcon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = SolidColor(Color.Black),
        ).build()
}

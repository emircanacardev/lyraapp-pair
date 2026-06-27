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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.data.player.PlaybackManager
import com.turkcell.lyraapp.data.player.PlayingTrack
import com.turkcell.lyraapp.ui.auth.complete.ProfileCompleteRoute
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.favorites.FavoritesRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.library.create.CreatePlaylistRoute
import com.turkcell.lyraapp.ui.library.detail.PlaylistDetailRoute
import com.turkcell.lyraapp.ui.player.PlayerRoute
import com.turkcell.lyraapp.ui.premium.payment.PaymentRoute
import com.turkcell.lyraapp.ui.premium.plan.PremiumRoute
import com.turkcell.lyraapp.ui.premium.success.PaymentSuccessRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.recentlyplayed.RecentlyPlayedRoute
import com.turkcell.lyraapp.ui.search.SearchRoute
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

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
                        onPlayPauseToggle = { playbackManager.setPlaying(!track.isPlaying) },
                        onFavoriteToggle = { playbackManager.toggleFavorite() },
                        onClick = {
                            navController.navigate(
                                "player?songId=${track.id}&title=${track.title}&artist=${track.artist}&startColor=${track.startColor}&endColor=${track.endColor}"
                            )
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
                    onNavigateToProfileComplete = {
                        navController.navigate(LyraDestination.ProfileComplete.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(LyraDestination.Register.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.ProfileComplete.route) {
                ProfileCompleteRoute(
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(LyraDestination.ProfileComplete.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
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
                    onNavigateToPlayer = { songId, title, artist, startColor, endColor ->
                        navController.navigate("player?songId=$songId&title=$title&artist=$artist&startColor=$startColor&endColor=$endColor")
                    },
                    onNavigateToPlaylist = { playlistId ->
                        navController.navigate(playlistDetailRoute(playlistId))
                    },
                    onNavigateToAllRecentlyPlayed = {
                        navController.navigate(LyraDestination.AllRecentlyPlayed.route)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(LyraDestination.Profile)
                    },
                )
            }
            composable(LyraDestination.AllRecentlyPlayed.route) {
                RecentlyPlayedRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { songId, title, artist, startColor, endColor ->
                        navController.navigate("player?songId=$songId&title=$title&artist=$artist&startColor=$startColor&endColor=$endColor")
                    },
                )
            }
            composable(LyraDestination.Search.route) { SearchRoute() }
            composable(LyraDestination.Library.route) { backStackEntry ->
                val playlistCreated = backStackEntry.savedStateHandle
                    .getStateFlow("playlist_created", false)
                    .collectAsStateWithLifecycle()

                LibraryRoute(
                    onOpenPlaylist = { id -> navController.navigate(playlistDetailRoute(id)) },
                    onCreatePlaylist = { navController.navigate(LyraDestination.CreatePlaylist.route) },
                    playlistCreated = playlistCreated.value,
                    onPlaylistCreatedConsumed = {
                        backStackEntry.savedStateHandle["playlist_created"] = false
                    },
                )
            }
            composable(LyraDestination.CreatePlaylist.route) {
                CreatePlaylistRoute(
                    onNavigateBack = { created ->
                        if (created) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("playlist_created", true)
                        }
                        navController.popBackStack()
                    }
                )
            }
            composable(LyraDestination.Favorites.route) {
                FavoritesRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(LyraDestination.Profile.route) {
                ProfileRoute(
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremium = {
                        navController.navigate(LyraDestination.Premium.route)
                    },
                )
            }
            composable(LyraDestination.Premium.route) {
                PremiumRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPayment = { planId, planType, planName, planPrice ->
                        navController.navigate(paymentRoute(planId, planType, planName, planPrice))
                    },
                )
            }
            composable(
                route = LyraDestination.Payment.route,
                arguments = listOf(
                    navArgument("planId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("planType") { type = NavType.StringType; defaultValue = "recurring" },
                    navArgument("planName") { type = NavType.StringType; defaultValue = "" },
                    navArgument("planPrice") { type = NavType.IntType; defaultValue = 0 },
                ),
            ) {
                PaymentRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSuccess = { planType ->
                        navController.navigate(paymentSuccessRoute(planType)) {
                            popUpTo(LyraDestination.Premium.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = LyraDestination.PaymentSuccess.route,
                arguments = listOf(
                    navArgument("planType") { type = NavType.StringType; defaultValue = "recurring" },
                ),
            ) {
                PaymentSuccessRoute(
                    onNavigateToHome = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
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
                    navArgument("songId") { type = NavType.StringType; nullable = true; defaultValue = "" },
                    navArgument("title") { type = NavType.StringType; nullable = true; defaultValue = "" },
                    navArgument("artist") { type = NavType.StringType; nullable = true; defaultValue = "" },
                    navArgument("startColor") { type = NavType.LongType; defaultValue = 0xFF8B6FB8L },
                    navArgument("endColor") { type = NavType.LongType; defaultValue = 0xFF4A3D6BL },
                ),
            ) {
                PlayerRoute(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Login.route) { inclusive = true }
        launchSingleTop = true
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = track.artist,
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
                        imageVector = if (track.isPlaying) LyraIcons.Pause else LyraIcons.Play,
                        contentDescription = if (track.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.onSurface,
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

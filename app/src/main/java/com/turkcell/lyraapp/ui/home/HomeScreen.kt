package com.turkcell.lyraapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.home.FeaturedPlaylist
import com.turkcell.lyraapp.data.home.ForYouSong
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.Recommendation
import com.turkcell.lyraapp.data.home.RecentlyPlayed
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun HomeRoute(
    onNavigateToPlayer: (songId: String, title: String, artist: String, startColor: Long, endColor: Long) -> Unit,
    onNavigateToPlaylist: (playlistId: String) -> Unit,
    onNavigateToAllRecentlyPlayed: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(HomeIntent.RefreshRecentlyPlayed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(HomeIntent.Retry)
                    }
                }
                is HomeEffect.NavigateToPlayer -> onNavigateToPlayer(
                    effect.songId,
                    effect.title,
                    effect.artist,
                    effect.startColor,
                    effect.endColor,
                )
                is HomeEffect.NavigateToPlaylist -> onNavigateToPlaylist(effect.playlistId)
                is HomeEffect.NavigateToAllRecentlyPlayed -> onNavigateToAllRecentlyPlayed()
                is HomeEffect.NavigateToProfile -> onNavigateToProfile()
            }
        }
    }

    HomeScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (state.isLoading && state.playlists.isEmpty() && state.recentlyPlayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    HomeHeader(
                        greeting = state.greeting,
                        userInitials = state.userInitials,
                        isDarkTheme = state.isDarkTheme,
                        onThemeToggle = { onIntent(HomeIntent.ToggleTheme) },
                        onAvatarClick = { onIntent(HomeIntent.AvatarClicked) },
                    )
                }

                if (state.recommendations.isNotEmpty()) {
                    item {
                        RecommendationsGrid(
                            recommendations = state.recommendations,
                            onItemClick = { song -> onIntent(HomeIntent.RecommendationSelected(song)) },
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "Son çalınanlar",
                        trailingText = if (state.recentlyPlayed.isNotEmpty()) "Tümü" else null,
                        onTrailingClick = if (state.recentlyPlayed.isNotEmpty()) {
                            { onIntent(HomeIntent.ShowAllRecentlyPlayed) }
                        } else null,
                    )
                }
                item {
                    RecentlyPlayedRow(
                        items = state.recentlyPlayed,
                        onItemClick = { song -> onIntent(HomeIntent.RecentlyPlayedSelected(song)) },
                    )
                }

                if (state.forYouSongs.isNotEmpty()) {
                    item { SectionHeader(title = "Senin için seçilenler") }
                    item {
                        ForYouSongsRow(
                            items = state.forYouSongs,
                            onItemClick = { song -> onIntent(HomeIntent.ForYouSongSelected(song)) },
                        )
                    }
                }

                if (state.playlists.isNotEmpty()) {
                    item { SectionHeader(title = "Çalma listelerim") }
                    item {
                        PlaylistsRow(
                            playlists = state.playlists,
                            onPlaylistClick = { id -> onIntent(HomeIntent.PlaylistSelected(id)) },
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    userInitials: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ne dinlemek istersin?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onThemeToggle) {
            Icon(
                imageVector = if (isDarkTheme) LyraIcons.LightMode else HomeIcons.Moon,
                contentDescription = "Temayı Değiştir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        UserAvatar(initials = userInitials, onClick = onAvatarClick)
    }
}

@Composable
private fun UserAvatar(initials: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = if (onTrailingClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onTrailingClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                } else Modifier,
            )
        }
    }
}

@Composable
private fun PlaylistsRow(
    playlists: List<FeaturedPlaylist>,
    onPlaylistClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id) },
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: FeaturedPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Artwork(
                startColor = playlist.artworkStartColor,
                endColor = playlist.artworkEndColor,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!playlist.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecentlyPlayedRow(
    items: List<RecentlyPlayed>,
    onItemClick: (RecentlyPlayed) -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Henüz şarkı çalmadınız",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onItemClick(item) },
            ) {
                Artwork(
                    startColor = item.artworkStartColor,
                    endColor = item.artworkEndColor,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ForYouSongsRow(
    items: List<ForYouSong>,
    onItemClick: (ForYouSong) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .clickable { onItemClick(item) },
            ) {
                Artwork(
                    startColor = item.artworkStartColor,
                    endColor = item.artworkEndColor,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecommendationsGrid(
    recommendations: List<Recommendation>,
    onItemClick: (Recommendation) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        recommendations.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    RecommendationCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    item: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            startColor = item.artworkStartColor,
            endColor = item.artworkEndColor,
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(Color(startColor), Color(endColor))))
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                ),
            ),
    )
}

private val previewState = HomeUiState(
    greeting = "İyi akşamlar",
    userInitials = "ZK",
    playlists = listOf(
        FeaturedPlaylist("pl-1", "Gece Sürüşü", "Sakin melodiler", 0xFF8B6FB8, 0xFF4A3D6B),
        FeaturedPlaylist("pl-2", "Sabah Enerjisi", null, 0xFF4AC2A8, 0xFF1F6E5C),
        FeaturedPlaylist("pl-3", "Focus Mode", "Odaklanma müziği", 0xFFD98E4A, 0xFF8A5526),
    ),
    recentlyPlayed = listOf(
        RecentlyPlayed("rp-1", "Neon Sokaklar", "Şehir Işıkları", 0xFFD98E4A, 0xFF8A5526),
        RecentlyPlayed("rp-2", "Derin Mavi", "Okyanus", 0xFF6FBF5A, 0xFF356B2A),
        RecentlyPlayed("rp-3", "Yıldız Tozu", "Polaris", 0xFF3D5A80, 0xFF1B2A45),
    ),
    forYouSongs = listOf(
        ForYouSong("s3", "Haftalık Keşif", "Aurora Drift", 0xFF9B7FC4, 0xFF5A4480),
        ForYouSong("s4", "Sakin Akşamlar", "City Pulse", 0xFF6B5FB8, 0xFF3A3270),
        ForYouSong("s5", "Enerji Ver", "Neon Wave", 0xFF3FAE9C, 0xFF1E5D52),
    ),
    recommendations = listOf(
        Recommendation("qp-1", "Gece Sürüşü", "Aurora Drift", 0xFF8B6FB8, 0xFF4A3D6B),
        Recommendation("qp-2", "Sabah Kahvesi", "City Pulse", 0xFF7C83D9, 0xFF3E4486),
        Recommendation("qp-3", "Neon Sokaklar", "Neon Wave", 0xFFD98E4A, 0xFF8A5526),
        Recommendation("qp-4", "Odaklan", "Aurora Drift", 0xFF4AC2A8, 0xFF1F6E5C),
    ),
)

@Preview(name = "Home - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Home - Light", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        HomeScreen(state = previewState, onIntent = {})
    }
}

private object HomeIcons {
    val Moon: ImageVector by lazy {
        ImageVector.Builder(
            name = "Moon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M12.3 22c5.36 0 9.7-4.34 9.7-9.7 0-3.32-1.67-6.26-4.22-8.02-.38-.26-.9-.1-.97.35-.37 2.4-2.18 4.38-4.63 4.9-2.42.5-4.47-.9-5.18-3.05-.15-.46-.7-.6-.92-.2C3.76 8.07 2 11.23 2 14.7 2 18.73 5.27 22 9.3 22c1 0 1.98-.2 2.87-.6.28-.13.25-.56.13-.6-.12-.04-.25-.06-.37-.06-.11 0-.21.01-.31.02-.15.01-.3.04-.46.04-.08 0-.16 0-.24 0z"
            ).toNodes(),
            fill = SolidColor(Color.Black),
        ).build()
    }
}

package com.turkcell.lyraapp.ui.favorites

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.favorites.Song
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import androidx.compose.foundation.layout.WindowInsets

/**
 * Favoriler (Beğenilen Şarkılar) akışının durumlu (stateful) giriş noktası.
 *
 * [FavoritesViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [FavoritesEffect]'leri tüketir.
 */
@Composable
fun FavoritesRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FavoritesEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(FavoritesIntent.Retry)
                    }
                }
            }
        }
    }

    FavoritesScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Favoriler (Beğenilen Şarkılar) ekranı.
 *
 * Tamamen durumsuzdur (stateless). Tasarımdaki çalma listesi başlığı, gradyan kalp kartı,
 * Oynat/Karıştır/İndir kontrol butonları ve çalan şarkı vurgulamasına sahip şarkı listesini barındırır.
 */
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Geri Butonu
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = LyraIcons.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (state.isLoading && state.songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Playlist Üst Kart ve Detayları
                    item {
                        PlaylistHeaderSection(
                            title = state.title,
                            subtitle = state.subtitle
                        )
                    }

                    // Kontrol Butonları
                    item {
                        PlaylistControlsSection(
                            isPlaying = state.isPlaying,
                            isShuffled = state.isShuffled,
                            isDownloaded = state.isDownloaded,
                            onPlayClick = { onIntent(FavoritesIntent.PlayClicked) },
                            onShuffleClick = { onIntent(FavoritesIntent.ShuffleClicked) },
                            onDownloadClick = { onIntent(FavoritesIntent.DownloadClicked) }
                        )
                    }

                    // Şarkı Listesi
                    items(state.songs, key = { it.id }) { song ->
                        val isCurrentPlaying = state.playingSongId == song.id
                        SongRowItem(
                            song = song,
                            isCurrentPlaying = isCurrentPlaying,
                            onSongClick = { onIntent(FavoritesIntent.SongClicked(song.id)) },
                            onFavoriteClick = { onIntent(FavoritesIntent.FavoriteClicked(song.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeaderSection(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gradyan Kalp Kartı
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFB1C8), Color(0xFFEFBD94))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = null,
                tint = Color(0xFF5E1133),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.width(20.dp))

        // Playlist Başlık ve Bilgileri
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaylistControlsSection(
    isPlaying: Boolean,
    isShuffled: Boolean,
    isDownloaded: Boolean,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Çal Butonu
        Button(
            onClick = onPlayClick,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) FavoritesIcons.Pause else FavoritesIcons.Play,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "Durdur" else "Çal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Karıştır Butonu
        IconButton(
            onClick = onShuffleClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isShuffled) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
        ) {
            Icon(
                imageVector = FavoritesIcons.Shuffle,
                contentDescription = "Karıştır",
                tint = if (isShuffled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // İndir Butonu
        IconButton(
            onClick = onDownloadClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isDownloaded) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
        ) {
            Icon(
                imageVector = if (isDownloaded) FavoritesIcons.CheckCircle else FavoritesIcons.Download,
                contentDescription = "İndir",
                tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    isCurrentPlaying: Boolean,
    onSongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentPlaying) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            )
            .clickable { onSongClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Şarkı Kapak Görseli
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(song.startColor), Color(song.endColor))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrentPlaying) {
                // Çalan Şarkı için Waveform (Ses Dalgası) Animasyonu
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 4.dp.toPx()
                    val gap = 4.dp.toPx()
                    val startX = (width - (3 * barWidth + 2 * gap)) / 2

                    // 3 adet ses çubuğu
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(startX, height * 0.4f),
                        size = androidx.compose.ui.geometry.Size(barWidth, height * 0.4f)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(startX + barWidth + gap, height * 0.25f),
                        size = androidx.compose.ui.geometry.Size(barWidth, height * 0.55f)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(startX + 2 * (barWidth + gap), height * 0.35f),
                        size = androidx.compose.ui.geometry.Size(barWidth, height * 0.45f)
                    )
                }
            } else if (song.hasPattern) {
                // Diğer Kapaklar için Hafif Dekoratif Yaylar
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = 20.dp.toPx(),
                        center = Offset(width * 0.8f, height * 0.8f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = 32.dp.toPx(),
                        center = Offset(width * 0.8f, height * 0.8f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Şarkı Metin Bilgileri
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrentPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Şarkı Süresi
        Text(
            text = song.duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Beğeni İkonu (Tıklandığında favoriden çıkarır)
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = "Beğenmekten Vazgeç",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Diğer Seçenekler İkonu (Üç Nokta)
        Icon(
            imageVector = FavoritesIcons.MoreVert,
            contentDescription = "Daha Fazla",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable { /* Diğer seçenekler tetiklenebilir */ }
        )
    }
}

/**
 * Favoriler ekranı için ihtiyaç duyulan yardımcı ikonlar.
 */
private object FavoritesIcons {
    val Play: ImageVector by lazy {
        lyraIcon("Play", "M8 5v14l11-7z")
    }

    val Pause: ImageVector by lazy {
        lyraIcon("Pause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z")
    }

    val Shuffle: ImageVector by lazy {
        lyraIcon(
            "Shuffle",
            "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.45 20 9.5V4h-5.5zm.73 11.09l-1.41 1.41 2.92 2.92L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z"
        )
    }

    val Download: ImageVector by lazy {
        lyraIcon(
            "Download",
            "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"
        )
    }

    val CheckCircle: ImageVector by lazy {
        lyraIcon(
            "CheckCircle",
            "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
        )
    }

    val MoreVert: ImageVector by lazy {
        lyraIcon(
            "MoreVert",
            "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
        )
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

@Preview(name = "Favorites - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenDarkPreview() {
    val previewState = FavoritesUiState(
        songs = listOf(
            Song("s-1", "Gece Yarısı", "Mavi Deniz", "3:34", 0xFF3FAE9C, 0xFF356B2A, hasPattern = true),
            Song("s-2", "Yıldız Tozu", "Polaris", "4:07", 0xFF2A5F73, 0xFF467B92, hasPattern = true)
        )
    )
    LyraAppTheme(darkTheme = true) {
        FavoritesScreen(state = previewState, onIntent = {}, onNavigateBack = {})
    }
}

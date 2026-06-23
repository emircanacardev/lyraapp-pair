package com.turkcell.lyraapp.ui.recentlyplayed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun RecentlyPlayedRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (songId: String, title: String, artist: String, startColor: Long, endColor: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecentlyPlayedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RecentlyPlayedEffect.NavigateToPlayer -> onNavigateToPlayer(
                    effect.songId,
                    effect.title,
                    effect.artist,
                    effect.startColor,
                    effect.endColor,
                )
            }
        }
    }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = err,
            actionLabel = "Tekrar dene",
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.onIntent(RecentlyPlayedIntent.Retry)
        }
    }

    RecentlyPlayedScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun RecentlyPlayedScreen(
    state: RecentlyPlayedUiState,
    onIntent: (RecentlyPlayedIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
        ) {
            item {
                RecentlyPlayedHeader(
                    songCount = state.songs.size,
                    onBack = onNavigateBack,
                )
            }

            when {
                state.isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.songs.isEmpty() && state.error == null -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Henüz şarkı çalmadınız",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Bir şarkı çalın ve burada görünsün",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                else -> itemsIndexed(state.songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        index = index + 1,
                        onClick = { onIntent(RecentlyPlayedIntent.SongSelected(song)) },
                    )
                    if (index < state.songs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 80.dp, end = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RecentlyPlayedHeader(
    songCount: Int,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Son Çalınanlar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (songCount > 0) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$songCount şarkı",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun SongRow(
    song: RecentlyPlayedSong,
    index: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(song.artworkStartColor), Color(song.artworkEndColor))
                    )
                )
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = LyraIcons.Play,
            contentDescription = "Oynat",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview(name = "Recently Played - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun RecentlyPlayedDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        RecentlyPlayedScreen(
            state = RecentlyPlayedUiState(
                isLoading = false,
                songs = listOf(
                    RecentlyPlayedSong("1", "Neon Tide", "Aurora Drift", 0xFF8B6FB8, 0xFF4A3D6B),
                    RecentlyPlayedSong("2", "City Lights", "City Pulse", 0xFF4AC2A8, 0xFF1F6E5C),
                    RecentlyPlayedSong("3", "Gece Sürüşü", "Neon Wave", 0xFFD98E4A, 0xFF8A5526),
                    RecentlyPlayedSong("4", "Derin Mavi", "Polaris", 0xFF3D5A80, 0xFF1B2A45),
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Recently Played - Light", showBackground = true, showSystemUi = true)
@Composable
private fun RecentlyPlayedLightPreview() {
    LyraAppTheme(darkTheme = false) {
        RecentlyPlayedScreen(
            state = RecentlyPlayedUiState(
                isLoading = false,
                songs = listOf(
                    RecentlyPlayedSong("1", "Neon Tide", "Aurora Drift", 0xFF8B6FB8, 0xFF4A3D6B),
                    RecentlyPlayedSong("2", "City Lights", "City Pulse", 0xFF4AC2A8, 0xFF1F6E5C),
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

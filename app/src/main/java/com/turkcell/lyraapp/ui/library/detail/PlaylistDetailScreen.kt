package com.turkcell.lyraapp.ui.library.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun PlaylistDetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PlaylistDetailEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    PlaylistDetailScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        PlaylistDetailTopBar(
            onBackClicked = { onIntent(PlaylistDetailIntent.BackClicked) },
        )
        LazyColumn {
            item {
                if (state.isLikedSongs) {
                    LikedSongsHeader(state = state, onIntent = onIntent)
                } else {
                    PlaylistHeader(state = state, onIntent = onIntent)
                }
            }
            items(items = state.songs, key = { it.id }) { song ->
                SongItemRow(
                    song = song,
                    onClick = { onIntent(PlaylistDetailIntent.SongClicked(song.id)) },
                    onLikeClicked = { onIntent(PlaylistDetailIntent.LikeSongClicked(song.id)) },
                    onMoreClicked = { onIntent(PlaylistDetailIntent.MoreSongClicked(song.id)) },
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlaylistDetailTopBar(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PlaylistHeader(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(state.coverColor)),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${state.ownerName} · ${state.songCount} şarkı · ${state.totalDuration}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        PlaylistActionRow(state = state, onIntent = onIntent)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PlaylistActionRow(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onIntent(PlaylistDetailIntent.LikePlaylistClicked) }) {
            Icon(
                imageVector = if (state.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = if (state.isLiked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = LyraIcons.Download,
                contentDescription = "İndir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = LyraIcons.Add,
                contentDescription = "Ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onIntent(PlaylistDetailIntent.ShuffleClicked) }) {
            Icon(
                imageVector = LyraIcons.Shuffle,
                contentDescription = "Karıştır",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onIntent(PlaylistDetailIntent.PlayClicked) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.PlayArrow,
                contentDescription = "Oynat",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun LikedSongsHeader(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(state.coverColor)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(52.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${state.songCount} şarkı · ${state.totalDuration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        LikedSongsActionRow(onIntent = onIntent)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LikedSongsActionRow(
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onIntent(PlaylistDetailIntent.PlayClicked) },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Çal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { onIntent(PlaylistDetailIntent.ShuffleClicked) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Shuffle,
                contentDescription = "Karıştır",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Download,
                contentDescription = "İndir",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SongItemRow(
    song: SongItem,
    onClick: () -> Unit,
    onLikeClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(song.thumbnailColor)),
            contentAlignment = Alignment.Center,
        ) {
            if (song.isPlaying) {
                Icon(
                    imageVector = LyraIcons.Waveform,
                    contentDescription = "Çalıyor",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (song.isPlaying) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Text(
            text = song.duration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        IconButton(
            onClick = onLikeClicked,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (song.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = if (song.isLiked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(
            onClick = onMoreClicked,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(name = "PlaylistDetail - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PlaylistDetailScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        PlaylistDetailScreen(
            state = PlaylistDetailUiState(
                playlistId = "2",
                title = "Gece Sürüşü",
                description = "Karanlık yollar için synth-pop",
                ownerName = "Zeynep Kaya",
                songCount = 5,
                totalDuration = "23 dk",
                coverColor = 0xFF7B5EA7L,
                songs = listOf(
                    SongItem("s1", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFF8B4513L, true, true),
                    SongItem("s2", "Gece Yarısı",   "Mavi Deniz",    "3:34", 0xFF2E8B57L, true),
                    SongItem("s3", "Mor Bulutlar",  "Derin Kaya",    "3:52", 0xFF8B5CF6L, false),
                ),
            ),
            onIntent = {},
        )
    }
}

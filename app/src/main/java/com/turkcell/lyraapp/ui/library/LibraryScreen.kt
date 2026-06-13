package com.turkcell.lyraapp.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun LibraryRoute(
    onOpenPlaylist: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.NavigateToSearch -> Unit
                is LibraryEffect.NavigateToCreatePlaylist -> onCreatePlaylist()
                is LibraryEffect.OpenPlaylist -> onOpenPlaylist(effect.id)
            }
        }
    }

    LibraryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        LibraryTopBar(
            onSearchClicked = { onIntent(LibraryIntent.SearchClicked) },
            onAddClicked = { onIntent(LibraryIntent.AddClicked) },
        )
        LibraryFilterRow(
            selectedFilter = state.selectedFilter,
            onFilterSelected = { onIntent(LibraryIntent.FilterSelected(it)) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        LibrarySortRow()
        if (state.selectedFilter == LibraryFilter.Playlists) {
            LazyColumn {
                items(items = state.playlists, key = { it.id }) { playlist ->
                    PlaylistItemRow(
                        item = playlist,
                        onClick = { onIntent(LibraryIntent.PlaylistClicked(playlist.id)) },
                        onMoreClicked = { onIntent(LibraryIntent.MoreClicked(playlist.id)) },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Henüz ${state.selectedFilter.label.lowercase()} eklenmedi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LibraryTopBar(
    onSearchClicked: () -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kütüphane",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onSearchClicked) {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onAddClicked) {
            Icon(
                imageVector = LyraIcons.Add,
                contentDescription = "Ekle",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LibraryFilterRow(
    selectedFilter: LibraryFilter,
    onFilterSelected: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = LyraIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySortRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LyraIcons.SwapVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Son eklenenler",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = LyraIcons.GridView,
                contentDescription = "Izgara görünümü",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PlaylistItemRow(
    item: PlaylistItem,
    onClick: () -> Unit,
    onMoreClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistThumbnail(item = item)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Çalma listesi · ${item.songCount} şarkı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.isPinned) {
            Icon(
                imageVector = LyraIcons.PushPin,
                contentDescription = "Sabitlenmiş",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        } else {
            IconButton(
                onClick = onMoreClicked,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = LyraIcons.MoreVert,
                    contentDescription = "Daha fazla seçenek",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaylistThumbnail(
    item: PlaylistItem,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(item.thumbnailColor)),
        contentAlignment = Alignment.Center,
    ) {
        if (item.isLikedSongs) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(name = "Library - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(
            state = LibraryUiState(
                playlists = listOf(
                    PlaylistItem("1", "Beğenilen Şarkılar", 5, isPinned = true,  thumbnailColor = 0xFFE91E8CL, isLikedSongs = true),
                    PlaylistItem("2", "Gece Sürüşü",        6, isPinned = false, thumbnailColor = 0xFF7B5EA7L),
                    PlaylistItem("3", "Sabah Kahvesi",       5, isPinned = false, thumbnailColor = 0xFF5B6AE8L),
                    PlaylistItem("4", "Odaklan",             5, isPinned = false, thumbnailColor = 0xFF26A69AL),
                    PlaylistItem("5", "Yaz Anıları",         5, isPinned = false, thumbnailColor = 0xFF4DD0E1L),
                    PlaylistItem("6", "Akustik Akşam",       4, isPinned = false, thumbnailColor = 0xFF00897BL),
                ),
            ),
            onIntent = {},
        )
    }
}

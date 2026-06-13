package com.turkcell.lyraapp.ui.search

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.search.Genre
import com.turkcell.lyraapp.data.search.PatternType
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Arama akışının durumlu (stateful) giriş noktası.
 *
 * [SearchViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [SearchEffect]'leri tüketir.
 */
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(SearchIntent.Retry)
                    }
                }
            }
        }
    }

    SearchScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Arama ekranı.
 *
 * Tamamen durumsuzdur (stateless). Tasarımdaki arama çubuğu, çip filtreleri ve
 * Canvas dekorasyonlarına sahip tür kartlarını içerir.
 */
@Composable
fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Başlık
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Ara",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Arama Çubuğu
            item(span = { GridItemSpan(2) }) {
                SearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Çip Filtreleri
            item(span = { GridItemSpan(2) }) {
                FilterChipsRow(
                    selectedFilter = state.selectedFilter,
                    availableFilters = state.availableFilters,
                    onFilterSelect = { onIntent(SearchIntent.FilterSelected(it)) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Yükleniyor veya Boş Durum Gösterimi
            if (state.isLoading && state.genres.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Bölüm Başlığı: Türlere Göz At
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Türlere göz at",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Tür Kartları Izgarası (Grid)
                items(state.filteredGenres, key = { it.id }) { genre ->
                    GenreCard(genre = genre)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Şarkı, sanatçı veya albüm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    availableFilters: List<String>,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 20.dp)
    ) {
        items(availableFilters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = "✓ ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(genre.startColor), Color(genre.endColor))
                )
            )
    ) {
        // Dekoratif Canvas Çizimleri
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (genre.patternType) {
                PatternType.CIRCLES -> {
                    // Akustik, Indie ve Klasik kartları için 2 yuvarlak
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 20.dp.toPx(),
                        center = Offset(width * 0.25f, height * 0.65f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 20.dp.toPx(),
                        center = Offset(width * 0.75f, height * 0.65f)
                    )
                }
                PatternType.CURVES -> {
                    // Lo-fi ve Jazz kartları için dalgalı konsantrik yaylar
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = 35.dp.toPx(),
                        center = Offset(width * 1.05f, height * 0.5f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = 55.dp.toPx(),
                        center = Offset(width * 1.05f, height * 0.5f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 75.dp.toPx(),
                        center = Offset(width * 1.05f, height * 0.5f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
                PatternType.NONE -> {
                    // Pop, Elektronik ve Yolculuk kartları için temiz görünüm veya hafif bir gradyan parlaması
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 60.dp.toPx(),
                        center = Offset(width * 0.5f, height * 0.5f)
                    )
                }
            }
        }

        // Tür Adı (Metin)
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Search - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenDarkPreview() {
    val previewState = SearchUiState(
        genres = listOf(
            Genre("g-1", "Pop", 0xFF3FAE9C, 0xFF8AD1C2, PatternType.NONE),
            Genre("g-2", "Elektronik", 0xFF8B6FB8, 0xFFB19CD9, PatternType.NONE),
            Genre("g-3", "Akustik", 0xFF6A5FB8, 0xFF8A7ED1, PatternType.CIRCLES),
            Genre("g-4", "Lo-fi", 0xFF2A5F73, 0xFF467B92, PatternType.CURVES)
        ),
        filteredGenres = listOf(
            Genre("g-1", "Pop", 0xFF3FAE9C, 0xFF8AD1C2, PatternType.NONE),
            Genre("g-2", "Elektronik", 0xFF8B6FB8, 0xFFB19CD9, PatternType.NONE),
            Genre("g-3", "Akustik", 0xFF6A5FB8, 0xFF8A7ED1, PatternType.CIRCLES),
            Genre("g-4", "Lo-fi", 0xFF2A5F73, 0xFF467B92, PatternType.CURVES)
        )
    )
    LyraAppTheme(darkTheme = true) {
        SearchScreen(state = previewState, onIntent = {})
    }
}

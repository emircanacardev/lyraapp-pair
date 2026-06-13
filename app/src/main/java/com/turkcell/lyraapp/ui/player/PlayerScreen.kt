package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.service.LyraMediaService
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Now Playing (Çalar) ekranının durumlu (stateful) giriş noktası.
 */
@Composable
fun PlayerRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlayerEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    PlayerScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBackgroundClicked = { LyraMediaService.start(context) },
        modifier = modifier,
    )
}

/**
 * Durumsuz Now Playing ekranı tasarımı.
 */
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    onBackgroundClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Kapaktan beslenen dikey gradyan arka plan: startColor -> startColor (yarı opak) -> Black
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(state.startColor),
            Color(state.startColor).copy(alpha = 0.5f),
            Color(0xFF0F0E0F) // Siyah tonu
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ÜST BAR: Kapatma Oku + Başlık + Seçenekler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onIntent(PlayerIntent.NavigateBack) }) {
                        Icon(
                            imageVector = PlayerIcons.ChevronDown,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ŞİMDİ ÇALIYOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Gece Vardiyası",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { /* Menü Tıklaması */ }) {
                        Icon(
                            imageVector = PlayerIcons.MoreVert,
                            contentDescription = "Daha Fazla",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ORTA BÖLÜM: Albüm Kapağı (Concentric Halka Efektli)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(1f)
                        .shadow(24.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(state.startColor), Color(state.endColor))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Konsantrik (Eşmerkezli) Çizgisel Halkalar Çizimi
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        // Merkez noktası sağ üstte hafif kaydırılmış
                        val centerOffset = Offset(width * 0.7f, height * 0.5f)

                        // 3 farklı çapta halka çizimi
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = width * 0.35f,
                            center = centerOffset,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = width * 0.55f,
                            center = centerOffset,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = width * 0.75f,
                            center = centerOffset,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ŞARKI BİLGİSİ: İsim, Sanatçı ve Beğeni Kalbi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = { onIntent(PlayerIntent.ToggleFavorite) }) {
                        Icon(
                            imageVector = if (state.isFavorite) PlayerIcons.Favorite else PlayerIcons.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (state.isFavorite) Color(0xFFF5B2C3) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ÇALMA ÇUBUĞU (SEEK BAR) ve ZAMAN ETİKETLERİ
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = state.progress,
                        onValueChange = { onIntent(PlayerIntent.ProgressChanged(it)) },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFFF5B2C3),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                            thumbColor = Color(0xFFF5B2C3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.currentTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = state.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // OYNATMA KONTROLLERİ: Karıştır, Önceki, Oynat/Duraklat, Sonraki, Tekrarla
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onIntent(PlayerIntent.ToggleShuffle) }) {
                        Icon(
                            imageVector = PlayerIcons.Shuffle,
                            contentDescription = "Karıştır",
                            tint = if (state.isShuffleEnabled) Color(0xFFF5B2C3) else Color.White.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(onClick = { onIntent(PlayerIntent.SkipPrevious) }) {
                        Icon(
                            imageVector = PlayerIcons.SkipPrevious,
                            contentDescription = "Önceki",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Büyük pembe Oynat/Duraklat butonu
                    IconButton(
                        onClick = { onIntent(PlayerIntent.TogglePlayPause) },
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFF5B2C3))
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) PlayerIcons.Pause else PlayerIcons.PlayArrow,
                            contentDescription = if (state.isPlaying) "Duraklat" else "Oynat",
                            tint = Color(0xFF2E171B), // Koyu kontrast
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = { onIntent(PlayerIntent.SkipNext) }) {
                        Icon(
                            imageVector = PlayerIcons.SkipNext,
                            contentDescription = "Sonraki",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = { onIntent(PlayerIntent.ToggleRepeat) }) {
                        Icon(
                            imageVector = PlayerIcons.Repeat,
                            contentDescription = "Tekrarla",
                            tint = if (state.isRepeatEnabled) Color(0xFFF5B2C3) else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ALT KONTROLLER: Cast, Arkaplan ve Liste kuyruğu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Cast */ }) {
                        Icon(
                            imageVector = PlayerIcons.Devices,
                            contentDescription = "Cihazlar",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Orta: Arkaplan (Alarm/Bildirim ve Yazı)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { onBackgroundClicked() }
                    ) {
                        Icon(
                            imageVector = PlayerIcons.BackgroundBell,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Arkaplan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(onClick = { /* Liste kuyruğu */ }) {
                        Icon(
                            imageVector = PlayerIcons.QueueMusic,
                            contentDescription = "Çalma Listesi",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Player ekranı için el ile çizilen çizim ikon tanımları.
 */
private object PlayerIcons {
    val ChevronDown: ImageVector by lazy {
        lyraIcon("ChevronDown", "M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z")
    }

    val MoreVert: ImageVector by lazy {
        lyraIcon("MoreVert", "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z")
    }

    val Favorite: ImageVector by lazy {
        lyraIcon("Favorite", "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z")
    }

    val FavoriteBorder: ImageVector by lazy {
        lyraIcon("FavoriteBorder", "M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5c0-3.08-2.42-5.5-5.5-5.5zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z")
    }

    val Shuffle: ImageVector by lazy {
        lyraIcon("Shuffle", "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z")
    }

    val SkipPrevious: ImageVector by lazy {
        lyraIcon("SkipPrevious", "M6 6h2v12H6zm3.5 6l8.5 6V6z")
    }

    val PlayArrow: ImageVector by lazy {
        lyraIcon("PlayArrow", "M8 5v14l11-7z")
    }

    val Pause: ImageVector by lazy {
        lyraIcon("Pause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z")
    }

    val SkipNext: ImageVector by lazy {
        lyraIcon("SkipNext", "M6 18l8.5-6L6 6v12zM16 6v12h2V6z")
    }

    val Repeat: ImageVector by lazy {
        lyraIcon("Repeat", "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z")
    }

    val Devices: ImageVector by lazy {
        lyraIcon("Devices", "M4 6h18V4H4c-1.1 0-2 .9-2 2v11H0v3h14v-3H4V6zm19 2h-6c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h6c.55 0 1-.45 1-1V9c0-.55-.45-1-1-1zm-1 9h-4v-7h4v7z")
    }

    val BackgroundBell: ImageVector by lazy {
        lyraIcon("BackgroundBell", "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z")
    }

    val QueueMusic: ImageVector by lazy {
        lyraIcon("QueueMusic", "M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z")
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

@Preview(name = "Player - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PlayerScreenDarkPreview() {
    val previewState = PlayerUiState(
        title = "Neon Sokaklar",
        subtitle = "Şehir Işıkları",
        startColor = 0xFFD98E4AL,
        endColor = 0xFF8A5526L
    )
    LyraAppTheme(darkTheme = true) {
        PlayerScreen(state = previewState, onIntent = {})
    }
}

package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.service.LyraMediaService
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun PlayerRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) LyraMediaService.start(context)
    }

    val onBackgroundClicked: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            LyraMediaService.start(context)
        }
    }

    PlayerScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(state.startColor),
            Color(state.startColor).copy(alpha = 0.5f),
            Color(0xFF0F0E0F)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = LyraIcons.ArrowBack,
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
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = PlayerIcons.MoreVert,
                            contentDescription = "Daha Fazla",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

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
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val centerOffset = Offset(width * 0.7f, height * 0.5f)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title.ifBlank { "Bilinmeyen Şarkı" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                when {
                    state.errorMessage != null -> ErrorContent(
                        message = state.errorMessage,
                        onRetry = { onIntent(PlayerIntent.Retry) },
                    )
                    else -> PlaybackControls(state = state, onIntent = onIntent)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlaybackControls(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val duration = state.durationMs.coerceAtLeast(0L)
    val positionFraction = if (duration > 0L) {
        (state.positionMs.toFloat() / duration).coerceIn(0f, 1f)
    } else 0f
    val sliderValue = if (isDragging) dragFraction else positionFraction

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = {
                isDragging = true
                dragFraction = it
            },
            onValueChangeFinished = {
                onIntent(PlayerIntent.SeekTo((dragFraction * duration).toLong()))
                isDragging = false
            },
            enabled = duration > 0L,
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
            val shownPosition = if (isDragging) (dragFraction * duration).toLong() else state.positionMs
            Text(
                text = formatTime(shownPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onIntent(PlayerIntent.Restart) }) {
            Icon(
                imageVector = LyraIcons.Restart,
                contentDescription = "Baştan başlat",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { onIntent(PlayerIntent.SeekBackward) }) {
            Icon(
                imageVector = LyraIcons.Rewind10,
                contentDescription = "10 saniye geri",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading || state.isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFF5B2C3)
                )
            } else {
                IconButton(
                    onClick = { onIntent(PlayerIntent.TogglePlayPause) },
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFF5B2C3))
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) LyraIcons.Pause else LyraIcons.Play,
                        contentDescription = if (state.isPlaying) "Duraklat" else "Oynat",
                        tint = Color(0xFF2E171B),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        IconButton(onClick = { onIntent(PlayerIntent.SeekForward) }) {
            Icon(
                imageVector = LyraIcons.Forward10,
                contentDescription = "10 saniye ileri",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        IconButton(onClick = { onIntent(PlayerIntent.Restart) }) {
            Icon(
                imageVector = PlayerIcons.Repeat,
                contentDescription = "Tekrarla",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text(text = "Tekrar dene", color = Color(0xFFF5B2C3))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private object PlayerIcons {
    val MoreVert: ImageVector by lazy {
        lyraIcon("MoreVert", "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z")
    }

    val Repeat: ImageVector by lazy {
        lyraIcon("Repeat", "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z")
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
        artist = "Şehir Işıkları",
        startColor = 0xFFD98E4AL,
        endColor = 0xFF8A5526L,
        isLoading = false,
        isPlaying = true,
        positionMs = 93_000L,
        durationMs = 223_000L,
    )
    LyraAppTheme(darkTheme = true) {
        PlayerScreen(state = previewState, onIntent = {}, onNavigateBack = {})
    }
}

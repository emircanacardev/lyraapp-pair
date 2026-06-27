package com.turkcell.lyraapp.ui.player

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.player.PlaybackManager
import com.turkcell.lyraapp.data.player.PlayingTrack
import com.turkcell.lyraapp.data.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val downloadRepository: DownloadRepository,
    private val playbackManager: PlaybackManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[ARG_SONG_ID]) {
        "PlayerViewModel requires '$ARG_SONG_ID' argument."
    }
    private val title: String = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val artist: String = savedStateHandle.get<String>(ARG_ARTIST).orEmpty()
    private val startColor: Long = savedStateHandle[ARG_START_COLOR] ?: 0xFF8B6FB8L
    private val endColor: Long = savedStateHandle[ARG_END_COLOR] ?: 0xFF4A3D6BL

    // Use the singleton player — not owned by this ViewModel
    private val player = playbackManager.player

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            title = title,
            artist = artist,
            startColor = startColor,
            endColor = endColor,
            isLoading = true,
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlayerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.update {
                it.copy(
                    isLoading = it.isLoading && playbackState == Player.STATE_BUFFERING && it.durationMs == 0L,
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    hasEnded = playbackState == Player.STATE_ENDED,
                )
            }
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isBuffering = false,
                    errorMessage = error.localizedMessage ?: "Sarki oynatılamadi.",
                )
            }
        }
    }

    init {
        val previousSongId = playbackManager.playingTrack.value?.id
        playbackManager.setTrack(
            PlayingTrack(
                id = songId,
                title = title,
                artist = artist,
                startColor = startColor,
                endColor = endColor,
            )
        )
        player.addListener(listener)
        checkDownloadStatus()
        loadAndPlay(previousSongId)
        observePosition()
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.TogglePlayPause -> {
                if (player.isPlaying) player.pause() else player.play()
            }
            PlayerIntent.Restart -> {
                player.seekTo(0L)
                player.play()
            }
            PlayerIntent.SeekForward -> seekBy(SEEK_STEP_MS)
            PlayerIntent.SeekBackward -> seekBy(-SEEK_STEP_MS)
            is PlayerIntent.SeekTo -> {
                val target = intent.positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
                player.seekTo(target)
            }
            PlayerIntent.Retry -> loadAndPlay()
            PlayerIntent.DownloadSong -> downloadSong()
        }
    }

    private fun checkDownloadStatus() {
        viewModelScope.launch {
            val downloaded = downloadRepository.isDownloaded(songId)
            _uiState.update { it.copy(isDownloaded = downloaded) }
        }
    }

    private fun downloadSong() {
        if (_uiState.value.isDownloaded || _uiState.value.isDownloading) return
        _uiState.update { it.copy(isDownloading = true) }
        viewModelScope.launch {
            downloadRepository.downloadSong(songId, title, artist)
                .onSuccess {
                    _uiState.update { it.copy(isDownloaded = true, isDownloading = false) }
                    _effect.send(PlayerEffect.ShowMessage("Sarki indirildi"))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDownloading = false) }
                    _effect.send(PlayerEffect.ShowMessage(error.message ?: "Indirme basarisiz oldu"))
                }
        }
    }

    private fun seekBy(deltaMs: Long) {
        val duration = player.duration.coerceAtLeast(0L)
        val target = (player.currentPosition + deltaMs).coerceIn(0L, duration)
        player.seekTo(target)
    }

    private fun loadAndPlay(previousSongId: String? = null) {
        // Same song already loaded in ExoPlayer — just sync UI, don't restart
        val alreadyPlaying = previousSongId == songId &&
            player.playbackState != Player.STATE_IDLE &&
            player.playbackState != Player.STATE_ENDED

        if (alreadyPlaying) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPlaying = player.isPlaying,
                    positionMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = player.duration.coerceAtLeast(0L),
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val localPath = downloadRepository.getLocalFilePath(songId)
            if (localPath != null) {
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(localPath))))
                player.prepare()
                player.playWhenReady = true
            } else {
                playerRepository.getStreamUrl(songId)
                    .onSuccess { url ->
                        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                        player.prepare()
                        player.playWhenReady = true
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Stream adresi alinamadi.",
                            )
                        }
                    }
            }
        }
    }

    private fun observePosition() {
        viewModelScope.launch {
            while (isActive) {
                val positionMs = player.currentPosition.coerceAtLeast(0L)
                val durationMs = player.duration.coerceAtLeast(0L)
                _uiState.update { it.copy(positionMs = positionMs, durationMs = durationMs) }
                delay(POSITION_POLL_MS)
            }
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        // Don't release — PlaybackManager owns the player, music continues in background
    }

    companion object {
        const val ARG_SONG_ID = "songId"
        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"
        const val ARG_START_COLOR = "startColor"
        const val ARG_END_COLOR = "endColor"

        private const val SEEK_STEP_MS = 10_000L
        private const val POSITION_POLL_MS = 500L
    }
}

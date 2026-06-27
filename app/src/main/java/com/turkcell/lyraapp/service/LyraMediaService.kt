package com.turkcell.lyraapp.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.turkcell.lyraapp.data.player.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LyraMediaService : Service() {

    @Inject lateinit var playbackManager: PlaybackManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        LyraNotificationHelper.createChannel(this)

        // Start foreground immediately with current track (avoids the ANR window)
        val track = playbackManager.playingTrack.value
        val notification = if (track != null)
            LyraNotificationHelper.build(this, track)
        else
            LyraNotificationHelper.build(this, com.turkcell.lyraapp.data.player.PlayingTrack(
                title = "LyraApp", artist = "Yükleniyor...",
                startColor = 0xFF8B6FB8L, endColor = 0xFF4A3D6BL
            ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(LyraNotificationHelper.NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(LyraNotificationHelper.NOTIFICATION_ID, notification)
        }

        observeTrack()
    }

    private fun observeTrack() {
        scope.launch {
            // Update notification on any track change (title, isPlaying, progress)
            playbackManager.playingTrack.collect { track ->
                if (track == null) {
                    stopSelf()
                    return@collect
                }
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(LyraNotificationHelper.NOTIFICATION_ID,
                    LyraNotificationHelper.build(this@LyraMediaService, track))
            }
        }

        // Throttle progress updates to every ~1 second to reduce overhead
        scope.launch {
            playbackManager.playingTrack
                .map { it?.positionMs }
                .distinctUntilChanged()
                .collect {
                    val track = playbackManager.playingTrack.value ?: return@collect
                    val nm = getSystemService(NotificationManager::class.java)
                    nm.notify(LyraNotificationHelper.NOTIFICATION_ID,
                        LyraNotificationHelper.build(this@LyraMediaService, track))
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                val player = playbackManager.player
                if (player.isPlaying) player.pause() else player.play()
            }
            ACTION_PREVIOUS -> playbackManager.player.seekTo(0L)
            ACTION_NEXT -> { /* playlist support eklenince burası dolar */ }
            ACTION_FAVORITE -> playbackManager.toggleFavorite()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        playbackManager.player.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PLAY_PAUSE = "com.turkcell.lyraapp.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.turkcell.lyraapp.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.turkcell.lyraapp.ACTION_NEXT"
        const val ACTION_FAVORITE = "com.turkcell.lyraapp.ACTION_FAVORITE"

        fun start(context: Context) =
            context.startForegroundService(Intent(context, LyraMediaService::class.java))

        fun stop(context: Context) =
            context.stopService(Intent(context, LyraMediaService::class.java))
    }
}

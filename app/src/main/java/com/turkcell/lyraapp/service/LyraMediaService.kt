package com.turkcell.lyraapp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder

class LyraMediaService : Service() {

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        LyraNotificationHelper.createChannel(this)
        mediaSession = MediaSession(this, "LyraMediaSession").apply {
            setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, 93_000L, 1f)
                    .setActions(
                        PlaybackState.ACTION_PLAY_PAUSE or
                                PlaybackState.ACTION_SKIP_TO_NEXT or
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS
                    )
                    .build()
            )
            setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Neon Sokaklar")
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Şehir Işıkları")
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, "Gece Vardiyası")
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, 223_000L)
                    .build()
            )
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = LyraNotificationHelper.build(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                LyraNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(LyraNotificationHelper.NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) =
            context.startForegroundService(Intent(context, LyraMediaService::class.java))

        fun stop(context: Context) =
            context.stopService(Intent(context, LyraMediaService::class.java))
    }
}

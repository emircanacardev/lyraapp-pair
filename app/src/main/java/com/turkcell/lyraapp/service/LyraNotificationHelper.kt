package com.turkcell.lyraapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.RemoteViews
import com.turkcell.lyraapp.MainActivity
import com.turkcell.lyraapp.R
import com.turkcell.lyraapp.data.player.PlayingTrack

internal object LyraNotificationHelper {

    const val CHANNEL_ID = "lyra_media_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "LyraApp Medya",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Çalan şarkı bildirimleri"
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun build(context: Context, track: PlayingTrack): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val progress = if (track.durationMs > 0)
            (track.positionMs * 100 / track.durationMs).toInt() else 0

        val albumArt = createGradientBitmap(track.startColor, track.endColor)

        val playPauseIcon = if (track.isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play

        val contentView = RemoteViews(context.packageName, R.layout.notification_player).apply {
            setTextViewText(R.id.notif_title, track.title)
            setTextViewText(R.id.notif_subtitle, track.artist)
            setImageViewBitmap(R.id.notif_album_art, albumArt)
            setProgressBar(R.id.notif_progress, 100, progress, false)
            setTextViewText(R.id.notif_current_time, formatMs(track.positionMs))
            setTextViewText(R.id.notif_duration, formatMs(track.durationMs))
            setImageViewResource(R.id.notif_btn_pause, playPauseIcon)
            setOnClickPendingIntent(R.id.notif_btn_favorite, serviceIntent(context, LyraMediaService.ACTION_FAVORITE))
            setOnClickPendingIntent(R.id.notif_btn_previous, serviceIntent(context, LyraMediaService.ACTION_PREVIOUS))
            setOnClickPendingIntent(R.id.notif_btn_pause, serviceIntent(context, LyraMediaService.ACTION_PLAY_PAUSE))
            setOnClickPendingIntent(R.id.notif_btn_next, serviceIntent(context, LyraMediaService.ACTION_NEXT))
        }

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_music)
            .setSubText("Şimdi çalıyor")
            .setContentTitle(track.title)
            .setContentIntent(openIntent)
            .setOngoing(track.isPlaying)
            .setColor(track.startColor.toInt())
            .setColorized(true)
            .setCustomBigContentView(contentView)
            .build()
    }

    private fun createGradientBitmap(startColor: Long, endColor: Long): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                startColor.toInt(), endColor.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        Canvas(bitmap).drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        return bitmap
    }

    private fun formatMs(ms: Long): String {
        val s = ms / 1000
        return "%d:%02d".format(s / 60, s % 60)
    }

    private fun serviceIntent(context: Context, action: String): PendingIntent =
        PendingIntent.getService(
            context, action.hashCode(),
            Intent(context, LyraMediaService::class.java).apply { this.action = action },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
}

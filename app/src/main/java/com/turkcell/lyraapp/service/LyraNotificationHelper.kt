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
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import com.turkcell.lyraapp.MainActivity
import com.turkcell.lyraapp.R

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

    fun build(context: Context, session: MediaSession): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Neon Sokaklar")
            .setContentText("Şehir Işıkları · Gece Vardiyası")
            .setLargeIcon(buildAlbumArt())
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(buildAction(context, android.R.drawable.btn_star, "Beğen", 1))
            .addAction(buildAction(context, android.R.drawable.ic_media_rew, "Önceki", 2))
            .addAction(buildAction(context, android.R.drawable.ic_media_pause, "Duraklat", 3))
            .addAction(buildAction(context, android.R.drawable.ic_media_ff, "Sonraki", 4))
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
            .build()
    }

    private fun buildAction(
        context: Context,
        iconRes: Int,
        title: String,
        requestCode: Int,
    ): Notification.Action = Notification.Action.Builder(
        Icon.createWithResource(context, iconRes),
        title,
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent("com.turkcell.lyraapp.NOOP"),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private fun buildAlbumArt(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                0xFFD98E4A.toInt(), 0xFF8A5526.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        Canvas(bmp).drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        return bmp
    }
}

package com.turkcell.lyraapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
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

    fun build(context: Context): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentView = RemoteViews(context.packageName, R.layout.notification_player).apply {
            setProgressBar(R.id.notif_progress, 100, 41, false)
            setOnClickPendingIntent(R.id.notif_btn_favorite, noopIntent(context, 1))
            setOnClickPendingIntent(R.id.notif_btn_previous, noopIntent(context, 2))
            setOnClickPendingIntent(R.id.notif_btn_pause, noopIntent(context, 3))
            setOnClickPendingIntent(R.id.notif_btn_next, noopIntent(context, 4))
        }

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_music)
            .setSubText("Şimdi çalıyor")
            .setContentTitle("Neon Sokaklar")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setColor(0xFFD98E4A.toInt())
            .setColorized(true)
            .setCustomBigContentView(contentView)
            .build()
    }

    private fun noopIntent(context: Context, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent("com.turkcell.lyraapp.NOOP"),
            PendingIntent.FLAG_IMMUTABLE
        )
}

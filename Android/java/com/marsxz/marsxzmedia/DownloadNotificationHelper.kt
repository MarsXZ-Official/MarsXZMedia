package com.marsxz.marsxzmedia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.math.roundToInt

object DownloadNotificationHelper {
    private const val CHANNEL_EVENTS_ID = "marsxz_media_events"
    private const val CHANNEL_EVENTS_NAME = "MarsXZ Media Events"

    private const val CHANNEL_DOWNLOADS_ID = "marsxz_media_downloads"
    private const val CHANNEL_DOWNLOADS_NAME = "MarsXZ Media Downloads"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val eventsChannel = NotificationChannel(
                CHANNEL_EVENTS_ID,
                CHANNEL_EVENTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Важные уведомления MarsXZ Media"
                enableVibration(true)
            }

            val downloadsChannel = NotificationChannel(
                CHANNEL_DOWNLOADS_ID,
                CHANNEL_DOWNLOADS_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Прогресс загрузок MarsXZ Media"
                enableVibration(false)
                setSound(null, null)
            }

            manager.createNotificationChannel(eventsChannel)
            manager.createNotificationChannel(downloadsChannel)
        }
    }

    fun showSimple(context: Context, title: String, message: String, id: Int? = null) {
        val notificationId = id ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val n = NotificationCompat.Builder(context, CHANNEL_EVENTS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun showDownloadStart(
        context: Context,
        notificationId: Int,
        fileName: String,
        isAudio: Boolean
    ) {
        val title = if (isAudio) "Скачивание аудио" else "Скачивание видео"
        val n = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("$title: $fileName")
            .setContentText("Подключение...")
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun updateDownloadProgress(
        context: Context,
        notificationId: Int,
        fileName: String,
        isAudio: Boolean,
        percent: Double,
        speed: String,
        eta: String,
        sizeInfo: String
    ) {
        val title = if (isAudio) "Скачивание аудио" else "Скачивание видео"

        val n = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("$title: $fileName")
            .setContentText("Скорость: $speed • Осталось: $eta")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Скорость: $speed\nОсталось: $eta\nСтатус: $sizeInfo\nЗавершено: ${"%.1f".format(percent)}%"
                )
            )
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(percent < 100.0)
            .setProgress(100, percent.roundToInt().coerceIn(0, 100), false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun showDownloadComplete(
        context: Context,
        notificationId: Int,
        fileName: String,
        isAudio: Boolean,
        success: Boolean,
        message: String = ""
    ) {
        val title = when {
            success && isAudio -> "✅ Аудио скачано"
            success && !isAudio -> "✅ Видео скачано"
            !success && isAudio -> "❌ Ошибка скачивания аудио"
            else -> "❌ Ошибка скачивания видео"
        }

        val text = if (success) fileName else message.ifBlank { "Неизвестная ошибка" }

        val n = NotificationCompat.Builder(context, CHANNEL_EVENTS_ID)
            .setSmallIcon(
                if (success) android.R.drawable.stat_sys_download_done
                else android.R.drawable.stat_notify_error
            )
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }
}

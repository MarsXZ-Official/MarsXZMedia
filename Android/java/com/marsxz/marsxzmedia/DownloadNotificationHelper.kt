package com.marsxz.marsxzmedia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.math.roundToInt

object DownloadNotificationHelper {
    private const val CHANNEL_EVENTS_ID = "marsxz_media_events_v15"
    private const val CHANNEL_EVENTS_NAME = "События MarsXZ Media"

    private const val CHANNEL_DOWNLOADS_ID = "marsxz_media_downloads_v2"
    private const val CHANNEL_DOWNLOADS_NAME = "MarsXZ Media Downloads"

    // Константные ID для определенных типов уведомлений
    const val NOTIFY_ID_SEARCH = 1001
    const val NOTIFY_ID_SETTINGS = 9101

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Канал для событий (Анализ, Готово, Ошибки) - ВЫСОКАЯ ВАЖНОСТЬ (Звук + Окна)
            if (manager.getNotificationChannel(CHANNEL_EVENTS_ID) == null) {
                val eventsChannel = NotificationChannel(
                    CHANNEL_EVENTS_ID,
                    CHANNEL_EVENTS_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Уведомления о статусе приложения (звук, вибрация, окна)"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(eventsChannel)
            }

            // Канал для прогресса загрузки - НИЗКИЙ ПРИОРИТЕТ (тихо, не трогаем)
            if (manager.getNotificationChannel(CHANNEL_DOWNLOADS_ID) == null) {
                val downloadsChannel = NotificationChannel(
                    CHANNEL_DOWNLOADS_ID,
                    CHANNEL_DOWNLOADS_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Прогресс загрузок MarsXZ Media"
                    setSound(null, null)
                    enableVibration(false)
                }
                manager.createNotificationChannel(downloadsChannel)
            }
        }
    }

    /**
     * Показывает уведомление о событии со звуком и окном.
     */
    fun showSimple(context: Context, title: String, message: String, id: Int? = null) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val notificationId = id ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val n = NotificationCompat.Builder(context, CHANNEL_EVENTS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun showDownloadStart(context: Context, notificationId: Int, fileName: String, isAudio: Boolean) {
        val title = if (isAudio) "Скачивание аудио" else "Скачивание видео"
        val n = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("$title: $fileName")
            .setContentText("Подключение...")
            .setSilent(true)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun updateDownloadProgress(context: Context, notificationId: Int, fileName: String, isAudio: Boolean, percent: Double, speed: String, eta: String, sizeInfo: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(fileName)
            .setContentText("Загрузка: ${percent.roundToInt()}%")
            .setSilent(true)
            .setOngoing(percent < 100.0)
            .setProgress(100, percent.roundToInt().coerceIn(0, 100), false)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, n)
    }

    fun showDownloadComplete(context: Context, notificationId: Int, fileName: String, isAudio: Boolean, success: Boolean, message: String = "") {
        ensureChannel(context)

        val title = if (success) "✅ Готово" else "❌ Ошибка"
        val text = if (success) "Файл сохранен: $fileName" else message
        val n = NotificationCompat.Builder(context, CHANNEL_EVENTS_ID)
            .setSmallIcon(if (success) android.R.drawable.stat_sys_download_done else android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, n)
    }
}

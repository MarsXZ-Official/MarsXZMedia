package com.marsxz.marsxzmedia

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build

object UiSoundPlayer {
    private var uiSoundPool: SoundPool? = null
    private var notifySoundPool: SoundPool? = null
    
    private var clickSoundId: Int = 0
    private var applySoundId: Int = 0
    
    private var clickLoaded = false
    private var applyLoaded = false

    fun init(context: Context) {
        if (uiSoundPool != null) return

        val uiAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        uiSoundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(uiAttrs)
            .build()

        val notifyAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        notifySoundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(notifyAttrs)
            .build()

        uiSoundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == clickSoundId) {
                clickLoaded = true
                AppLog.write(context, "I", "Звук click загружен", "Sound")
            }
        }
        
        notifySoundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == applySoundId) {
                applyLoaded = true
                AppLog.write(context, "I", "Звук apply загружен", "Sound")
            }
        }

        // Загружаем ресурсы
        try {
            clickSoundId = uiSoundPool!!.load(context, R.raw.click, 1)
            applySoundId = notifySoundPool!!.load(context, R.raw.apply, 1)
        } catch (e: Exception) {
            AppLog.write(context, "E", "Ошибка загрузки звуков: ${e.message}", "Sound")
        }
    }

    private fun getSoundTheme(context: Context): String {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("sound_theme", "system") ?: "system"
    }

    fun playClick(context: Context) {
        // Клик работает ТОЛЬКО если выбрана тема Minecraft
        if (getSoundTheme(context) == "minecraft" && clickLoaded) {
            uiSoundPool?.play(clickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playApply(context: Context) {
        val theme = getSoundTheme(context)
        
        if (theme == "minecraft") {
            if (applyLoaded) {
                notifySoundPool?.play(applySoundId, 1f, 1f, 2, 0, 1f)
            }
        } else {
            // СИСТЕМНЫЙ РЕЖИМ
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                ringtone?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.isLooping = false
                    it.play()
                }
            } catch (e: Exception) {
                AppLog.write(context, "W", "Не удалось проиграть системный звук", "Sound")
            }

            // Вибрация всегда в системном режиме
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
        }
    }

    fun release() {
        uiSoundPool?.release()
        notifySoundPool?.release()
        uiSoundPool = null
        notifySoundPool = null
        clickLoaded = false
        applyLoaded = false
    }
}

package com.marsxz.marsxzmedia

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object UiSoundPlayer {
    private var soundPool: SoundPool? = null
    private var loaded = false

    private var clickSoundId: Int = 0
    private var applySoundId: Int = 0

    fun init(context: Context) {
        if (soundPool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        var loadedCount = 0
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loadedCount++
                if (loadedCount >= 2) {
                    loaded = true
                }
            }
        }

        clickSoundId = soundPool!!.load(context, R.raw.click, 1)
        applySoundId = soundPool!!.load(context, R.raw.apply, 1)
    }

    fun playClick() {
        if (!loaded) return
        soundPool?.play(clickSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playApply() {
        if (!loaded) return
        soundPool?.play(applySoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
        clickSoundId = 0
        applySoundId = 0
    }
}
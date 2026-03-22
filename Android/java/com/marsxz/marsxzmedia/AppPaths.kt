package com.marsxz.marsxzmedia

import android.content.Context
import java.io.File

object AppPaths {
    private const val DOWNLOADS_FOLDER = "Downloads"
    private const val AUDIO_FOLDER = "Audio"
    private const val VIDEO_FOLDER = "Video"
    private const val LOGS_FOLDER = "Logs"
    private const val TEMP_FOLDER = "Temp"

    fun publicRoot(context: Context): File =
        context.getExternalFilesDir(null) ?: context.filesDir

    fun downloadsRoot(context: Context): File = File(publicRoot(context), DOWNLOADS_FOLDER)

    fun audioVideoDefaultAudioDir(context: Context): File =
        File(downloadsRoot(context), AUDIO_FOLDER)

    fun audioVideoDefaultVideoDir(context: Context): File =
        File(downloadsRoot(context), VIDEO_FOLDER)

    fun tempDir(context: Context): File = File(context.cacheDir, TEMP_FOLDER)

    fun logsDir(context: Context): File = File(context.filesDir, LOGS_FOLDER)

    fun ensureDirectories(context: Context) {
        listOf(
            downloadsRoot(context),
            audioVideoDefaultAudioDir(context),
            audioVideoDefaultVideoDir(context),
            tempDir(context),
            logsDir(context)
        ).forEach {
            if (!it.exists()) it.mkdirs()
        }
    }
}
package com.marsxz.marsxzmedia

import android.content.Context
import java.io.File

object FFmpegBinaryManager {
    private const val FFMPEG_LIB_NAME = "libffmpeg_exec.so"

    fun getExecutable(context: Context): File {
        val nativeDirPath = context.applicationInfo.nativeLibraryDir
        val nativeDir = File(nativeDirPath)
        val ffmpeg = File(nativeDir, FFMPEG_LIB_NAME)

        if (!ffmpeg.exists()) {
            val files = nativeDir.list()?.sorted()?.joinToString().orEmpty()
            throw IllegalStateException(
                "FFmpeg не найден в nativeLibraryDir: ${ffmpeg.absolutePath}. Доступно: $files"
            )
        }

        ffmpeg.setReadable(true, false)
        ffmpeg.setExecutable(true, false)
        return ffmpeg
    }
}

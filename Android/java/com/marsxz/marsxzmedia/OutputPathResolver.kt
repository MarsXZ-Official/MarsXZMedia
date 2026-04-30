package com.marsxz.marsxzmedia

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object OutputPathResolver {
    private const val PREFS = "app_settings"
    private const val PRIMARY_ROOT = "/storage/emulated/0"

    fun useDefaultPath(context: Context): Boolean {
        return prefs(context).getBoolean("use_default_path", true)
    }

    fun useSeparatePaths(context: Context): Boolean {
        return !useDefaultPath(context) && prefs(context).getBoolean("separate_paths", false)
    }

    fun noSubfolders(context: Context): Boolean {
        return prefs(context).getBoolean("no_subfolders", false)
    }

    fun currentVideoDir(context: Context): File {
        if (useDefaultPath(context)) return AppPaths.audioVideoDefaultVideoDir(context)

        val base = manualOrFallbackBase(
            prefs(context).getString("video_path_text", null)
        )

        return if (noSubfolders(context)) {
            ensureDir(base)
        } else {
            ensureDir(File(base, "Video"))
        }
    }

    fun currentAudioDir(context: Context): File {
        if (useDefaultPath(context)) return AppPaths.audioVideoDefaultAudioDir(context)

        val raw = if (useSeparatePaths(context)) {
            prefs(context).getString("music_path_text", null)
        } else {
            prefs(context).getString("video_path_text", null)
        }

        val base = manualOrFallbackBase(raw)

        return if (noSubfolders(context)) {
            ensureDir(base)
        } else {
            ensureDir(File(base, "Audio"))
        }
    }

    fun manualOrFallbackBase(raw: String?): File {
        val normalized = normalizeUserPath(raw)
        return File(normalized)
    }

    fun normalizeUserPath(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return PRIMARY_ROOT

        val cleaned = value.replace('\\', '/').trim()

        return when {
            cleaned.startsWith(PRIMARY_ROOT) -> cleaned
            cleaned.startsWith("/") -> cleaned
            else -> "$PRIMARY_ROOT/${cleaned.trimStart('/')}"
        }
    }

    fun hasDirectSharedStorageAccess(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()
    }

    private fun ensureDir(file: File): File {
        if (!file.exists()) file.mkdirs()
        return file
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
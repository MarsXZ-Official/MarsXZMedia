package com.marsxz.marsxzmedia

import android.content.Context
import java.io.File
import java.util.concurrent.TimeUnit

object LogMaintenance {
    private const val PREFS = "app_settings"

    fun combinedLogFile(context: Context): File {
        AppPaths.ensureDirectories(context)
        return File(AppPaths.logsDir(context), "combined_app.log")
    }

    fun enforcePolicy(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val disableLogs = prefs.getBoolean("disable_logs", false)
            val infiniteLogs = prefs.getBoolean("infinite_logs", true)
            val maxDays = prefs.getInt("max_log_days", 365).coerceAtLeast(1)
            val logFile = combinedLogFile(context)

            if (!logFile.exists()) return

            if (disableLogs) {
                return
            }

            if (!infiniteLogs) {
                val ageMs = System.currentTimeMillis() - logFile.lastModified()
                val maxAgeMs = TimeUnit.DAYS.toMillis(maxDays.toLong())
                if (ageMs > maxAgeMs) {
                    logFile.delete()
                }
            }
        } catch (_: Exception) {
        }
    }
}

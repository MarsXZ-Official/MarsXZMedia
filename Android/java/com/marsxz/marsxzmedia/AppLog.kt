package com.marsxz.marsxzmedia

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLog {
    private const val PREFS = "app_settings"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private fun logFile(context: Context): File = File(AppPaths.logsDir(context), "combined_app.log")

    private fun isFileLoggingDisabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("disable_logs", false)
    }

    fun write(
        context: Context,
        level: String,
        message: String,
        tag: String = "Main",
        error: Throwable? = null
    ) {
        try {
            if (!isFileLoggingDisabled(context)) {
                AppPaths.ensureDirectories(context)
                val line = buildString {
                    append("[")
                    append(dateFormat.format(Date()))
                    append("] [")
                    append(tag)
                    append("] [")
                    append(level)
                    append("] ")
                    append(message)
                    if (error != null) {
                        append("\n")
                        append(Log.getStackTraceString(error))
                    }
                    append("\n")
                }
                logFile(context).appendText(line, Charsets.UTF_8)
            }
        } catch (_: Exception) {
        }

        when (level.uppercase(Locale.ROOT)) {
            "E" -> Log.e(tag, message, error)
            "W" -> Log.w(tag, message, error)
            "D" -> Log.d(tag, message, error)
            else -> Log.i(tag, message, error)
        }
    }
}

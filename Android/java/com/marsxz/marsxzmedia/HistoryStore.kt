package com.marsxz.marsxzmedia

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object HistoryStore {
    private const val MAX_ENTRIES = 200
    private const val HISTORY_FILE_NAME = "history.json"

    private fun historyFile(context: Context): File {
        return File(context.filesDir, HISTORY_FILE_NAME)
    }

    fun loadAll(context: Context): List<HistoryEntry> {
        return try {
            val file = historyFile(context)
            if (!file.exists()) return emptyList()

            val json = file.readText()
            val array = JSONArray(json)
            val result = mutableListOf<HistoryEntry>()

            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                result += HistoryEntry(
                    timestamp = obj.optLong("timestamp", 0L),
                    title = obj.optString("title", "Без названия"),
                    url = obj.optString("url", "")
                )
            }

            result.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, title: String?, url: String?) {
        val trimmedUrl = url?.trim().orEmpty()
        if (trimmedUrl.isBlank()) return

        val trimmedTitle = title?.trim().takeUnless { it.isNullOrBlank() } ?: "Без названия"

        val entries = loadAll(context).toMutableList()
        entries.add(
            0,
            HistoryEntry(
                timestamp = System.currentTimeMillis(),
                title = trimmedTitle,
                url = trimmedUrl
            )
        )

        save(context, entries.take(MAX_ENTRIES))
    }

    fun clear(context: Context) {
        try {
            val file = historyFile(context)
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }

    private fun save(context: Context, entries: List<HistoryEntry>) {
        try {
            val file = historyFile(context)
            file.parentFile?.mkdirs()

            val array = JSONArray()
            entries.forEach { entry ->
                val obj = JSONObject().apply {
                    put("timestamp", entry.timestamp)
                    put("title", entry.title)
                    put("url", entry.url)
                }
                array.put(obj)
            }

            file.writeText(array.toString(2))
        } catch (_: Exception) {
        }
    }
}
package com.marsxz.marsxzmedia

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GoogleHttpTranslator {

    fun translateTitleSync(text: String, targetLang: String?): String {
        if (text.isBlank()) return text
        if (targetLang.isNullOrBlank()) return text

        return try {
            val langCode = targetLang.substringBefore('-').trim().lowercase()
            if (langCode.isBlank()) return text

            val encoded = URLEncoder.encode(text, Charsets.UTF_8.name())
            val url = URL(
                "https://translate.googleapis.com/translate_a/single" +
                        "?client=gtx&sl=auto&tl=$langCode&dt=t&ie=UTF-8&oe=UTF-8&q=$encoded"
            )

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            val json = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            connection.disconnect()

            parseTranslatedText(json).ifBlank { text }
        } catch (_: Exception) {
            text
        }
    }

    private fun parseTranslatedText(json: String): String {
        val root = JSONArray(json)
        if (root.length() == 0) return ""

        val parts = root.optJSONArray(0) ?: return ""
        val sb = StringBuilder()

        for (i in 0 until parts.length()) {
            val part = parts.optJSONArray(i) ?: continue
            val segment = part.optString(0)
            if (segment.isNotBlank()) sb.append(segment)
        }

        return sb.toString().trim()
    }
}
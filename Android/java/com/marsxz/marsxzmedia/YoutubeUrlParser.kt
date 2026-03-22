package com.marsxz.marsxzmedia

import java.net.URI

object YoutubeUrlParser {
    fun extractVideoId(input: String): String? {
        val text = input.trim()
        if (text.isBlank()) return null

        val simple = Regex("^[A-Za-z0-9_-]{11}$")
        if (simple.matches(text)) return text

        return try {
            val uri = URI(text)
            val host = (uri.host ?: "").lowercase()
            val path = uri.path ?: ""
            val query = uri.rawQuery ?: ""

            when {
                host.endsWith("youtu.be") -> path.trim('/').takeIf { it.length >= 11 }?.substring(0, 11)
                host.contains("youtube.com") -> when {
                    path.startsWith("/watch") -> parseQuery(query)["v"]?.takeIf { it.length >= 11 }?.substring(0, 11)
                    path.startsWith("/shorts/") -> path.removePrefix("/shorts/").trim('/').takeIf { it.length >= 11 }?.substring(0, 11)
                    path.startsWith("/embed/") -> path.removePrefix("/embed/").trim('/').takeIf { it.length >= 11 }?.substring(0, 11)
                    else -> null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseQuery(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split('&').mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null else part.substring(0, idx) to part.substring(idx + 1)
        }.toMap()
    }
}

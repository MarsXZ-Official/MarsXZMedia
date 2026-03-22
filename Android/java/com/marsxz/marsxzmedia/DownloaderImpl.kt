package com.marsxz.marsxzmedia

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DownloaderImpl : Downloader() {
    override fun execute(request: Request): Response {
        val connection = URL(request.url()).openConnection() as HttpURLConnection
        connection.requestMethod = request.httpMethod()
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
        )

        request.headers().forEach { (name, values) ->
            connection.setRequestProperty(name, values.joinToString(", "))
        }

        request.dataToSend()?.let { data ->
            connection.doOutput = true
            connection.outputStream.use { os -> os.write(data) }
        }

        val responseCode = connection.responseCode
        val bodyText = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)?.let { stream ->
            BufferedReader(InputStreamReader(stream)).use { it.readText() }
        } ?: ""

        val latestUrl = connection.url.toString()
        val headers = connection.headerFields
            .filterKeys { it != null }
            .mapValues { entry -> entry.value ?: emptyList() }

        val responseMessage = connection.responseMessage ?: ""
        connection.disconnect()

        return Response(responseCode, responseMessage, headers, bodyText, latestUrl)
    }
}

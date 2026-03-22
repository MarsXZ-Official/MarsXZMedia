package com.marsxz.marsxzmedia

import android.content.Context
import java.io.File

object MediaConverter {
    fun convertAudioToMp3(
        context: Context,
        inputFile: File,
        desiredTitle: String,
        requestedBitrateKbps: Int?
    ): File {
        val ffmpeg = FFmpegBinaryManager.getExecutable(context)
        val outputFile = uniqueTargetFile(
            dir = OutputPathResolver.currentAudioDir(context),
            title = desiredTitle,
            ext = "mp3"
        )
        val bitrate = (requestedBitrateKbps ?: 320).coerceIn(64, 320)

        val attempts = listOf(
            listOf(
                "-y",
                "-i", inputFile.absolutePath,
                "-vn",
                "-c:a", "libmp3lame",
                "-b:a", "${bitrate}k",
                "-ar", "44100",
                outputFile.absolutePath
            ),
            listOf(
                "-y",
                "-i", inputFile.absolutePath,
                "-vn",
                "-c:a", "mp3",
                "-b:a", "${bitrate}k",
                "-ar", "44100",
                outputFile.absolutePath
            )
        )

        runFfmpegAttempts(
            context = context,
            ffmpegBinary = ffmpeg,
            attempts = attempts,
            failureHint = "Проверьте поддержку MP3-кодека в сборке FFmpeg."
        )

        if (!outputFile.exists() || outputFile.length() <= 0L) {
            throw IllegalStateException("FFmpeg не создал MP3-файл")
        }

        inputFile.delete()
        return outputFile
    }

    fun convertVideoToMp4(
        context: Context,
        videoInputFile: File,
        audioInputFile: File?,
        desiredTitle: String,
        requestedHeight: Int?,
        requestedAudioBitrateKbps: Int?
    ): File {
        val ffmpeg = FFmpegBinaryManager.getExecutable(context)
        val outputFile = uniqueTargetFile(
            dir = OutputPathResolver.currentVideoDir(context),
            title = desiredTitle,
            ext = "mp4"
        )
        val audioBitrate = (requestedAudioBitrateKbps ?: 192).coerceIn(96, 320)

        val attempts = listOf(
            buildVideoArgs(
                videoInputFile = videoInputFile,
                audioInputFile = audioInputFile,
                outputFile = outputFile,
                requestedHeight = requestedHeight,
                videoEncoder = "libx264",
                videoExtra = listOf("-preset", "veryfast", "-crf", "23"),
                audioEncoder = "aac",
                audioBitrate = audioBitrate
            ),
            buildVideoArgs(
                videoInputFile = videoInputFile,
                audioInputFile = audioInputFile,
                outputFile = outputFile,
                requestedHeight = requestedHeight,
                videoEncoder = "mpeg4",
                videoExtra = listOf("-q:v", "5"),
                audioEncoder = "aac",
                audioBitrate = audioBitrate
            )
        )

        runFfmpegAttempts(
            context = context,
            ffmpegBinary = ffmpeg,
            attempts = attempts,
            failureHint = "Проверьте поддержку H.264/AAC или MPEG4/AAC в сборке FFmpeg."
        )

        if (!outputFile.exists() || outputFile.length() <= 0L) {
            throw IllegalStateException("FFmpeg не создал MP4-файл")
        }

        videoInputFile.delete()
        audioInputFile?.delete()
        return outputFile
    }

    private fun buildVideoArgs(
        videoInputFile: File,
        audioInputFile: File?,
        outputFile: File,
        requestedHeight: Int?,
        videoEncoder: String,
        videoExtra: List<String>,
        audioEncoder: String,
        audioBitrate: Int
    ): List<String> {
        val args = mutableListOf(
            "-y",
            "-i", videoInputFile.absolutePath
        )

        if (audioInputFile != null) {
            args += listOf("-i", audioInputFile.absolutePath)
            args += listOf("-map", "0:v:0", "-map", "1:a:0")
        } else {
            args += listOf("-map", "0:v:0")
        }

        if (requestedHeight != null) {
            args += listOf("-vf", "scale=-2:$requestedHeight")
        }

        args += listOf("-c:v", videoEncoder)
        args += videoExtra

        if (audioInputFile != null) {
            args += listOf("-c:a", audioEncoder, "-b:a", "${audioBitrate}k")
        } else {
            args += listOf("-an")
        }

        args += listOf("-movflags", "+faststart", outputFile.absolutePath)
        return args
    }

    private fun runFfmpegAttempts(
        context: Context,
        ffmpegBinary: File,
        attempts: List<List<String>>,
        failureHint: String
    ) {
        val nativeLibDir = File(context.applicationInfo.nativeLibraryDir)
        val cxxShared = File(nativeLibDir, "libc++_shared.so")

        if (!ffmpegBinary.exists()) {
            throw IllegalStateException("FFmpeg не найден: ${ffmpegBinary.absolutePath}")
        }

        if (!cxxShared.exists()) {
            throw IllegalStateException(
                "Не найдена зависимость FFmpeg: ${cxxShared.absolutePath}. " +
                        "Скопируйте libc++_shared.so в jniLibs для нужного ABI."
            )
        }

        var lastExitCode = -1
        var lastOutput = ""

        attempts.forEachIndexed { index, args ->
            val command = listOf(ffmpegBinary.absolutePath) + args
            AppLog.write(
                context,
                "I",
                "FFmpeg попытка ${index + 1}: ${command.joinToString(" ")}",
                "FFmpeg"
            )

            val processBuilder = ProcessBuilder(command)
                .directory(nativeLibDir)
                .redirectErrorStream(true)

            val env = processBuilder.environment()

            // Важно: чтобы executable видел libc++_shared.so и другие .so рядом
            env["LD_LIBRARY_PATH"] = nativeLibDir.absolutePath

            // Иногда помогает на некоторых устройствах/сборках
            env["PATH"] = nativeLibDir.absolutePath + ":" + (env["PATH"] ?: "")

            val process = processBuilder.start()

            val outputText = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (outputText.isNotBlank()) {
                AppLog.write(
                    context,
                    if (exitCode == 0) "I" else "E",
                    outputText.takeLast(3500),
                    "FFmpeg"
                )
            }

            if (exitCode == 0) return

            lastExitCode = exitCode
            lastOutput = outputText
        }

        val details = lastOutput.takeLast(800).ifBlank { "Нет вывода FFmpeg" }
        throw IllegalStateException(
            "FFmpeg завершился с кодом $lastExitCode. $failureHint Последний вывод: $details"
        )
    }

    private fun uniqueTargetFile(dir: File, title: String, ext: String): File {
        val safeBase = sanitizeFileName(title).ifBlank {
            if (ext == "mp3") "Audio" else "Video"
        }

        var file = File(dir, "$safeBase.$ext")
        var counter = 2

        while (file.exists()) {
            file = File(dir, "$safeBase ($counter).$ext")
            counter++
        }

        return file
    }

    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("""[\\/:*?\"<>|]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
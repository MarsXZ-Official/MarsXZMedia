package com.marsxz.marsxzmedia

data class VideoInfo(
    val videoId: String,
    val title: String,
    val description: String,
    val author: String,
    val durationText: String,
    val thumbnailUrl: String?
)

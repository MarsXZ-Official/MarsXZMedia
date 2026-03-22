package com.marsxz.marsxzmedia

data class VideoFormatsInfo(
    val maxVideoHeight: Int?,
    val qualityItems: List<QualityItem>,
    val audioTracks: List<String>
) {
    data class QualityItem(
        val label: String,
        val height: Int
    )
}

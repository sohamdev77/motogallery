package com.motogallery.app.data.model

data class MediaMetadata(
    val title: String = "",
    val path: String = "",
    val sizeBytes: Long = 0L,
    val mimeType: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val dateTakenFormatted: String = "",
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val aperture: String? = null,
    val shutterSpeed: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val flash: String? = null,
    val whiteBalance: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val durationFormatted: String? = null,
    val bitrate: String? = null
)

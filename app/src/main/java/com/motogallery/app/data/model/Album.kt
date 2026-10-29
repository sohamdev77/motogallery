package com.motogallery.app.data.model

import android.net.Uri

enum class AlbumType {
    CAMERA,
    SCREENSHOTS,
    DOWNLOADS,
    VIDEOS,
    FAVORITES,
    RECENT,
    DEVICE_FOLDER,
    CUSTOM
}

data class Album(
    val id: String,
    val name: String,
    val coverUri: Uri? = null,
    val itemCount: Int = 0,
    val type: AlbumType = AlbumType.DEVICE_FOLDER,
    val bucketId: String? = null
)

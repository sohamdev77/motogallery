package com.motogallery.app.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String = "",
    val displayName: String = "",
    val mimeType: String = "",
    val size: Long = 0L,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val dateTaken: Long = 0L,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val isVideo: Boolean = false,
    val isFavorite: Boolean = false,
    val bucketId: String = "",
    val bucketDisplayName: String = "",
    val orientation: Int = 0
) {
    val bestTimestamp: Long
        get() = when {
            dateTaken > 0L -> dateTaken
            dateModified > 0L -> dateModified * 1000L
            dateAdded > 0L -> dateAdded * 1000L
            else -> 0L
        }
}

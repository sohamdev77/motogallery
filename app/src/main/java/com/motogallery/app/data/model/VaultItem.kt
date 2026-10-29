package com.motogallery.app.data.model

data class VaultItem(
    val id: Long = 0L,
    val originalName: String,
    val encryptedFileName: String,
    val encryptedThumbnailName: String? = null,
    val mimeType: String,
    val size: Long,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val isVideo: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val isTrashed: Boolean = false,
    val trashedTimestamp: Long = 0L
) {
    val daysRemainingInTrash: Int
        get() {
            if (!isTrashed || trashedTimestamp <= 0L) return 30
            val elapsedMs = System.currentTimeMillis() - trashedTimestamp
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
            val remainingMs = thirtyDaysMs - elapsedMs
            return (remainingMs / (24 * 60 * 60 * 1000L)).coerceAtLeast(0).toInt()
        }
}

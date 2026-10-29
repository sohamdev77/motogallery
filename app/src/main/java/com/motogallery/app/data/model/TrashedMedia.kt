package com.motogallery.app.data.model

import android.net.Uri

data class TrashedMediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val isVideo: Boolean,
    val trashedTimeMs: Long,
    val isVaultItem: Boolean = false,
    val vaultItem: VaultItem? = null
) {
    val daysRemaining: Int
        get() {
            if (isVaultItem && vaultItem != null) {
                return vaultItem.daysRemainingInTrash
            }
            if (trashedTimeMs <= 0L) return 30
            val elapsedMs = System.currentTimeMillis() - trashedTimeMs
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
            val remainingMs = thirtyDaysMs - elapsedMs
            return (remainingMs / (24 * 60 * 60 * 1000L)).coerceAtLeast(0).toInt()
        }
}

package com.motogallery.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.motogallery.app.data.model.VaultItem

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val originalName: String,
    val encryptedFileName: String,
    val encryptedThumbnailName: String?,
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
    fun toVaultItem(): VaultItem {
        return VaultItem(
            id = id,
            originalName = originalName,
            encryptedFileName = encryptedFileName,
            encryptedThumbnailName = encryptedThumbnailName,
            mimeType = mimeType,
            size = size,
            durationMs = durationMs,
            width = width,
            height = height,
            isVideo = isVideo,
            dateAdded = dateAdded,
            isTrashed = isTrashed,
            trashedTimestamp = trashedTimestamp
        )
    }

    companion object {
        fun fromVaultItem(item: VaultItem): VaultItemEntity {
            return VaultItemEntity(
                id = item.id,
                originalName = item.originalName,
                encryptedFileName = item.encryptedFileName,
                encryptedThumbnailName = item.encryptedThumbnailName,
                mimeType = item.mimeType,
                size = item.size,
                durationMs = item.durationMs,
                width = item.width,
                height = item.height,
                isVideo = item.isVideo,
                dateAdded = item.dateAdded,
                isTrashed = item.isTrashed,
                trashedTimestamp = item.trashedTimestamp
            )
        }
    }
}

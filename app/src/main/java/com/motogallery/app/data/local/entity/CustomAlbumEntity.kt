package com.motogallery.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_albums")
data class CustomAlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "custom_album_items",
    primaryKeys = ["albumId", "mediaId"]
)
data class CustomAlbumItemEntity(
    val albumId: Long,
    val mediaId: Long,
    val uriString: String,
    val addedAt: Long = System.currentTimeMillis()
)

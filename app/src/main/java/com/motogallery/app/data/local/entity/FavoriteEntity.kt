package com.motogallery.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val mediaId: Long,
    val uriString: String,
    val dateFavorited: Long = System.currentTimeMillis()
)

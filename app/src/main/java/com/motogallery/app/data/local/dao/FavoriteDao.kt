package com.motogallery.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.motogallery.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT mediaId FROM favorites")
    fun getAllFavoriteIdsFlow(): Flow<List<Long>>

    @Query("SELECT mediaId FROM favorites")
    suspend fun getAllFavoriteIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    suspend fun isFavorite(mediaId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun deleteFavorite(mediaId: Long)

    @Query("DELETE FROM favorites WHERE mediaId IN (:mediaIds)")
    suspend fun deleteFavorites(mediaIds: List<Long>)
}

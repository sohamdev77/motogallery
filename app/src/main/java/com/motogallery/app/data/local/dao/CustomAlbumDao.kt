package com.motogallery.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.motogallery.app.data.local.entity.CustomAlbumEntity
import com.motogallery.app.data.local.entity.CustomAlbumItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomAlbumDao {
    @Query("SELECT * FROM custom_albums ORDER BY createdAt DESC")
    fun getAllAlbumsFlow(): Flow<List<CustomAlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: CustomAlbumEntity): Long

    @Query("DELETE FROM custom_albums WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: Long)

    @Query("DELETE FROM custom_album_items WHERE albumId = :albumId")
    suspend fun deleteAlbumItems(albumId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CustomAlbumItemEntity>)

    @Query("DELETE FROM custom_album_items WHERE albumId = :albumId AND mediaId IN (:mediaIds)")
    suspend fun removeItemsFromAlbum(albumId: Long, mediaIds: List<Long>)

    @Query("SELECT mediaId FROM custom_album_items WHERE albumId = :albumId")
    fun getMediaIdsForAlbumFlow(albumId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM custom_album_items WHERE albumId = :albumId")
    suspend fun getItemCountForAlbum(albumId: Long): Int

    @Query("SELECT uriString FROM custom_album_items WHERE albumId = :albumId ORDER BY addedAt DESC LIMIT 1")
    suspend fun getCoverUriForAlbum(albumId: Long): String?
}

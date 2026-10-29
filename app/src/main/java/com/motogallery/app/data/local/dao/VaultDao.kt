package com.motogallery.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.motogallery.app.data.local.entity.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items WHERE isTrashed = 0 ORDER BY dateAdded DESC")
    fun getAllActiveItemsFlow(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE isTrashed = 0 ORDER BY dateAdded DESC")
    suspend fun getAllActiveItems(): List<VaultItemEntity>

    @Query("SELECT * FROM vault_items WHERE isTrashed = 1 ORDER BY trashedTimestamp DESC")
    fun getAllTrashedItemsFlow(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE isTrashed = 1 ORDER BY trashedTimestamp DESC")
    suspend fun getAllTrashedItems(): List<VaultItemEntity>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long

    @Update
    suspend fun updateItem(item: VaultItemEntity)

    @Query("UPDATE vault_items SET isTrashed = 1, trashedTimestamp = :trashedTime WHERE id = :id")
    suspend fun moveToTrash(id: Long, trashedTime: Long = System.currentTimeMillis())

    @Query("UPDATE vault_items SET isTrashed = 1, trashedTimestamp = :trashedTime WHERE id IN (:ids)")
    suspend fun moveMultipleToTrash(ids: List<Long>, trashedTime: Long = System.currentTimeMillis())

    @Query("UPDATE vault_items SET isTrashed = 0, trashedTimestamp = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("UPDATE vault_items SET isTrashed = 0, trashedTimestamp = 0 WHERE id IN (:ids)")
    suspend fun restoreMultipleFromTrash(ids: List<Long>)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM vault_items WHERE id IN (:ids)")
    suspend fun deleteMultiplePermanently(ids: List<Long>)

    @Query("SELECT * FROM vault_items WHERE isTrashed = 1 AND trashedTimestamp < :beforeTimestamp")
    suspend fun getExpiredTrashItems(beforeTimestamp: Long): List<VaultItemEntity>

    @Query("DELETE FROM vault_items WHERE isTrashed = 1 AND trashedTimestamp < :beforeTimestamp")
    suspend fun deleteExpiredTrash(beforeTimestamp: Long)

    @Query("SELECT SUM(size) FROM vault_items")
    suspend fun getTotalVaultStorageBytes(): Long?
}

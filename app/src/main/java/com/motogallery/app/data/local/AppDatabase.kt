package com.motogallery.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.motogallery.app.data.local.dao.CustomAlbumDao
import com.motogallery.app.data.local.dao.FavoriteDao
import com.motogallery.app.data.local.dao.VaultDao
import com.motogallery.app.data.local.entity.CustomAlbumEntity
import com.motogallery.app.data.local.entity.CustomAlbumItemEntity
import com.motogallery.app.data.local.entity.FavoriteEntity
import com.motogallery.app.data.local.entity.VaultItemEntity

@Database(
    entities = [
        FavoriteEntity::class,
        CustomAlbumEntity::class,
        CustomAlbumItemEntity::class,
        VaultItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun customAlbumDao(): CustomAlbumDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motogallery.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

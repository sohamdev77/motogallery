package com.motogallery.app

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.motogallery.app.data.local.AppDatabase
import com.motogallery.app.data.preferences.AppPreferences
import com.motogallery.app.data.vault.VaultCryptoManager
import com.motogallery.app.data.vault.VaultLockManager
import com.motogallery.app.data.vault.VaultManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MotoGalleryApp : Application(), ImageLoaderFactory {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val preferences by lazy { AppPreferences(this) }
    val vaultCryptoManager by lazy { VaultCryptoManager(this) }
    val vaultLockManager by lazy { VaultLockManager() }
    val vaultManager by lazy {
        VaultManager(
            context = this,
            vaultDao = database.vaultDao(),
            cryptoManager = vaultCryptoManager,
            vaultLockManager = vaultLockManager
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Run background startup housekeeping (e.g. purge expired 30-day vault trash)
        applicationScope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                vaultManager.purgeExpiredTrash(thirtyDaysAgo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB cache
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: MotoGalleryApp
            private set
    }
}

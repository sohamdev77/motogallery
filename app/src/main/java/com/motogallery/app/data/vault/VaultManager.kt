package com.motogallery.app.data.vault

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.motogallery.app.data.local.dao.VaultDao
import com.motogallery.app.data.local.entity.VaultItemEntity
import com.motogallery.app.data.model.VaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class VaultManager(
    private val context: Context,
    private val vaultDao: VaultDao,
    private val cryptoManager: VaultCryptoManager,
    val vaultLockManager: VaultLockManager
) {

    private val vaultDir: File by lazy {
        File(context.filesDir, "vault_storage").apply { if (!exists()) mkdirs() }
    }

    private val thumbDir: File by lazy {
        File(vaultDir, "thumbnails").apply { if (!exists()) mkdirs() }
    }

    val activeItemsFlow: Flow<List<VaultItem>> = vaultDao.getAllActiveItemsFlow().map { entities ->
        entities.map { it.toVaultItem() }
    }

    val trashedItemsFlow: Flow<List<VaultItem>> = vaultDao.getAllTrashedItemsFlow().map { entities ->
        entities.map { it.toVaultItem() }
    }

    suspend fun getActiveItems(): List<VaultItem> = withContext(Dispatchers.IO) {
        vaultDao.getAllActiveItems().map { it.toVaultItem() }
    }

    suspend fun getItemById(id: Long): VaultItem? = withContext(Dispatchers.IO) {
        vaultDao.getItemById(id)?.toVaultItem()
    }

    /**
     * Imports public media into the encrypted vault:
     * 1. Encrypts file bytes into app-private storage.
     * 2. Generates and encrypts an in-memory thumbnail.
     * 3. Verifies decryption integrity before confirming import.
     * 4. Persists metadata in Room database.
     */
    suspend fun importMedia(
        sourceUri: Uri,
        originalName: String,
        mimeType: String,
        isVideo: Boolean,
        size: Long,
        durationMs: Long = 0L,
        width: Int = 0,
        height: Int = 0
    ): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val encFileName = "enc_${UUID.randomUUID()}.enc"
            val targetEncFile = File(vaultDir, encFileName)

            // Step 1: Encrypt payload from MediaStore content stream
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetEncFile).use { output ->
                    cryptoManager.encryptStream(input, output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open source media stream: $sourceUri"))

            // Step 2: Generate thumbnail and encrypt it
            val thumbFileName = "thumb_${UUID.randomUUID()}.enc"
            val targetThumbFile = File(thumbDir, thumbFileName)
            val thumbGenerated = generateAndEncryptThumbnail(sourceUri, isVideo, targetThumbFile)

            // Step 3: Integrity verification (Attempt decrypting first 1KB or test read)
            val testBuffer = ByteArray(1024)
            var verified = false
            try {
                FileInputStream(targetEncFile).use { fis ->
                    ByteArrayOutputStream().use { baos ->
                        cryptoManager.decryptStream(fis, baos)
                        verified = baos.size() > 0
                    }
                }
            } catch (e: Exception) {
                // Decryption verification failed, delete corrupted file
                targetEncFile.delete()
                targetThumbFile.delete()
                return@withContext Result.failure(Exception("Integrity check failed after encryption: ${e.message}"))
            }

            if (!verified) {
                targetEncFile.delete()
                targetThumbFile.delete()
                return@withContext Result.failure(Exception("Verification returned empty decrypted stream"))
            }

            // Step 4: Save to Room
            val entity = VaultItemEntity(
                originalName = originalName,
                encryptedFileName = encFileName,
                encryptedThumbnailName = if (thumbGenerated) thumbFileName else null,
                mimeType = mimeType,
                size = size,
                durationMs = durationMs,
                width = width,
                height = height,
                isVideo = isVideo,
                dateAdded = System.currentTimeMillis()
            )
            val id = vaultDao.insertItem(entity)
            val item = entity.copy(id = id).toVaultItem()

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateAndEncryptThumbnail(uri: Uri, isVideo: Boolean, targetEncThumbFile: File): Boolean {
        return try {
            val bitmap = if (isVideo) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val frame = retriever.getFrameAtTime(1_000_000) // 1 second in
                retriever.release()
                frame
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                    val sampleSize = calculateInSampleSize(options, 512, 512)
                    context.contentResolver.openInputStream(uri)?.use { secondStream ->
                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                        }
                        BitmapFactory.decodeStream(secondStream, null, decodeOptions)
                    }
                }
            }

            if (bitmap != null) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                bitmap.recycle()
                val encryptedThumb = cryptoManager.encryptBytes(baos.toByteArray())
                targetEncThumbFile.writeBytes(encryptedThumb)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Decrypts encrypted file directly into in-memory bytes for display.
     */
    suspend fun decryptToMemory(item: VaultItem): ByteArray = withContext(Dispatchers.IO) {
        val file = File(vaultDir, item.encryptedFileName)
        require(file.exists()) { "Encrypted file not found on disk" }
        FileInputStream(file).use { fis ->
            ByteArrayOutputStream().use { baos ->
                cryptoManager.decryptStream(fis, baos)
                baos.toByteArray()
            }
        }
    }

    /**
     * Decrypts thumbnail to in-memory bytes.
     */
    suspend fun decryptThumbnailToMemory(item: VaultItem): ByteArray? = withContext(Dispatchers.IO) {
        val thumbName = item.encryptedThumbnailName ?: return@withContext null
        val file = File(thumbDir, thumbName)
        if (!file.exists()) return@withContext null
        try {
            val encBytes = file.readBytes()
            cryptoManager.decryptBytes(encBytes)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Restores media from the private vault back to the public gallery.
     */
    suspend fun restoreToPublicGallery(item: VaultItem): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val encFile = File(vaultDir, item.encryptedFileName)
            if (!encFile.exists()) return@withContext Result.failure(Exception("Encrypted file missing"))

            val collectionUri = if (item.isVideo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }

            val relativePath = if (item.isVideo) {
                "${Environment.DIRECTORY_MOVIES}/MotoGallery"
            } else {
                "${Environment.DIRECTORY_PICTURES}/MotoGallery"
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, item.originalName)
                put(MediaStore.MediaColumns.MIME_TYPE, item.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val targetUri = context.contentResolver.insert(collectionUri, values)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                FileInputStream(encFile).use { input ->
                    cryptoManager.decryptStream(input, output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream for restoration"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(targetUri, values, null, null)
            }

            // Successfully restored; shred and remove from vault
            deletePermanently(item.id)

            Result.success(targetUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveToTrash(id: Long) = withContext(Dispatchers.IO) {
        vaultDao.moveToTrash(id)
    }

    suspend fun moveMultipleToTrash(ids: List<Long>) = withContext(Dispatchers.IO) {
        vaultDao.moveMultipleToTrash(ids)
    }

    suspend fun restoreFromTrash(id: Long) = withContext(Dispatchers.IO) {
        vaultDao.restoreFromTrash(id)
    }

    suspend fun restoreMultipleFromTrash(ids: List<Long>) = withContext(Dispatchers.IO) {
        vaultDao.restoreMultipleFromTrash(ids)
    }

    suspend fun deletePermanently(id: Long) = withContext(Dispatchers.IO) {
        val item = vaultDao.getItemById(id) ?: return@withContext
        val encFile = File(vaultDir, item.encryptedFileName)
        cryptoManager.secureShredFile(encFile)
        item.encryptedThumbnailName?.let {
            val thumbFile = File(thumbDir, it)
            cryptoManager.secureShredFile(thumbFile)
        }
        vaultDao.deletePermanently(id)
    }

    suspend fun deleteMultiplePermanently(ids: List<Long>) = withContext(Dispatchers.IO) {
        for (id in ids) {
            deletePermanently(id)
        }
    }

    suspend fun purgeExpiredTrash(beforeTimestamp: Long) = withContext(Dispatchers.IO) {
        val expired = vaultDao.getExpiredTrashItems(beforeTimestamp)
        for (entity in expired) {
            deletePermanently(entity.id)
        }
    }

    suspend fun getTotalVaultSizeBytes(): Long = withContext(Dispatchers.IO) {
        vaultDao.getTotalVaultStorageBytes() ?: 0L
    }
}

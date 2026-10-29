package com.motogallery.app.data.mediastore

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.motogallery.app.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

sealed class MediaOperationResult {
    data class Success(val affectedCount: Int = 1, val targetUri: Uri? = null) : MediaOperationResult()
    data class RequiresConsent(val intentSender: IntentSender) : MediaOperationResult()
    data class Error(val message: String, val throwable: Throwable? = null) : MediaOperationResult()
}

class MediaOperationsHelper(private val context: Context) {

    private val contentResolver = context.contentResolver

    /**
     * Request system consent to trash or untrash media (Android 11+).
     * If trash API is not supported (Android < 11), falls back to permanent delete.
     */
    suspend fun requestTrashMedia(uris: List<Uri>, trash: Boolean = true): MediaOperationResult = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext MediaOperationResult.Success(0)

        val resolvedUris = uris.map { resolveToMediaStoreUri(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createTrashRequest(contentResolver, resolvedUris, trash)
                MediaOperationResult.RequiresConsent(pendingIntent.intentSender)
            } catch (e: Exception) {
                MediaOperationResult.Error("Failed to create trash request: ${e.message}", e)
            }
        } else {
            // Android 10 and below doesn't have system MediaStore trash, fallback to delete
            requestDeleteMedia(resolvedUris)
        }
    }

    /**
     * Request deletion of media items.
     * Attempts direct deletion for app-owned or unconstrained files, and requests
     * system authorization via MediaStore.createDeleteRequest for Scoped Storage items.
     */
    suspend fun requestDeleteMedia(
        uris: List<Uri>,
        paths: List<String> = emptyList()
    ): MediaOperationResult = withContext(Dispatchers.IO) {
        if (uris.isEmpty() && paths.isEmpty()) return@withContext MediaOperationResult.Success(0)

        var directlyDeleted = 0
        val remainingUris = mutableListOf<Uri>()

        // 1. Try deleting physical files if paths are provided
        for (path in paths) {
            try {
                val f = java.io.File(path)
                if (f.exists() && f.delete()) {
                    directlyDeleted++
                }
            } catch (_: Exception) {}
        }

        // 2. Try direct ContentResolver deletion first (succeeds if owned by app or pre-Android 10)
        for (rawUri in uris) {
            val uri = resolveToMediaStoreUri(rawUri)
            try {
                val rows = contentResolver.delete(uri, null, null)
                if (rows > 0) {
                    directlyDeleted += rows
                } else {
                    remainingUris.add(uri)
                }
            } catch (e: SecurityException) {
                remainingUris.add(uri)
            } catch (e: Exception) {
                remainingUris.add(uri)
            }
        }

        if (remainingUris.isEmpty()) {
            return@withContext MediaOperationResult.Success(directlyDeleted)
        }

        // 3. For remaining items needing system consent on Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createDeleteRequest(contentResolver, remainingUris)
                MediaOperationResult.RequiresConsent(pendingIntent.intentSender)
            } catch (e: Exception) {
                MediaOperationResult.Error("Failed to create delete request: ${e.message}", e)
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Android 10 RecoverableSecurityException
            try {
                var deleted = 0
                for (uri in remainingUris) {
                    deleted += contentResolver.delete(uri, null, null)
                }
                MediaOperationResult.Success(directlyDeleted + deleted)
            } catch (e: SecurityException) {
                if (e is RecoverableSecurityException) {
                    MediaOperationResult.RequiresConsent(e.userAction.actionIntent.intentSender)
                } else {
                    MediaOperationResult.Error("SecurityException during deletion: ${e.message}", e)
                }
            } catch (e: Exception) {
                MediaOperationResult.Error("Error during deletion: ${e.message}", e)
            }
        } else {
            // Android 9 and below
            try {
                var deleted = 0
                for (uri in remainingUris) {
                    deleted += contentResolver.delete(uri, null, null)
                }
                MediaOperationResult.Success(directlyDeleted + deleted)
            } catch (e: Exception) {
                MediaOperationResult.Error("Failed to delete media: ${e.message}", e)
            }
        }
    }

    /**
     * Resolves potentially non-standard URIs (such as Android PhotoPicker URIs)
     * to standard MediaStore Content URIs where possible.
     */
    fun resolveToMediaStoreUri(uri: Uri): Uri {
        val uriStr = uri.toString()
        if (uriStr.startsWith("content://media/external/")) {
            return uri
        }

        // Android PhotoPicker URI: content://media/picker/... or content://...photopicker...
        if (uri.authority?.contains("photopicker") == true || uriStr.contains("/picker/")) {
            val lastSegment = uri.lastPathSegment
            val id = lastSegment?.toLongOrNull()
            if (id != null) {
                // Check if it exists in Images or Video
                val mime = try { contentResolver.getType(uri) } catch (_: Exception) { null }
                return if (mime?.startsWith("video/") == true) {
                    android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                } else {
                    android.content.ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                }
            }
        }
        return uri
    }

    /**
     * Renames a media item in MediaStore.
     */
    suspend fun renameMedia(item: MediaItem, newNameWithExt: String): MediaOperationResult = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newNameWithExt)
            }
            val rows = contentResolver.update(item.uri, values, null, null)
            if (rows > 0) {
                MediaOperationResult.Success(rows)
            } else {
                MediaOperationResult.Error("Failed to rename file")
            }
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                MediaOperationResult.RequiresConsent(e.userAction.actionIntent.intentSender)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val pendingIntent = MediaStore.createWriteRequest(contentResolver, listOf(item.uri))
                    MediaOperationResult.RequiresConsent(pendingIntent.intentSender)
                } catch (writeEx: Exception) {
                    MediaOperationResult.Error("Permission required to rename: ${writeEx.message}", writeEx)
                }
            } else {
                MediaOperationResult.Error("Permission denied to rename: ${e.message}", e)
            }
        } catch (e: Exception) {
            MediaOperationResult.Error("Error renaming file: ${e.message}", e)
        }
    }

    /**
     * Copies a media item to a target relative folder (e.g. Pictures/MotoGallery or Pictures/CustomAlbum).
     */
    suspend fun copyMedia(item: MediaItem, targetRelativeFolder: String): MediaOperationResult = withContext(Dispatchers.IO) {
        try {
            val targetCollection = if (item.isVideo) {
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

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, item.displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, item.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, targetRelativeFolder)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val targetUri = contentResolver.insert(targetCollection, values)
                ?: return@withContext MediaOperationResult.Error("Could not create destination entry")

            contentResolver.openInputStream(item.uri)?.use { inStream ->
                contentResolver.openOutputStream(targetUri)?.use { outStream ->
                    inStream.copyTo(outStream)
                }
            } ?: return@withContext MediaOperationResult.Error("Could not stream media data")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(targetUri, values, null, null)
            }

            MediaOperationResult.Success(1, targetUri)
        } catch (e: Exception) {
            MediaOperationResult.Error("Failed to copy media: ${e.message}", e)
        }
    }
}

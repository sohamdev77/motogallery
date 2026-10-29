package com.motogallery.app.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.motogallery.app.data.model.Album
import com.motogallery.app.data.model.AlbumType
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.TrashedMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context) {

    private val contentResolver = context.contentResolver

    /**
     * Observes MediaStore images and videos reactively.
     * Re-queries automatically when photos or videos are added, modified, or deleted on device.
     */
    fun observeMedia(): Flow<List<MediaItem>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(queryAllMedia())
            }
        }

        trySend(queryAllMedia())

        val imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val videosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        contentResolver.registerContentObserver(imagesUri, true, observer)
        contentResolver.registerContentObserver(videosUri, true, observer)

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Queries all accessible public photos and videos on the device.
     */
    fun queryAllMedia(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT
        )

        val imageProjection = projection + arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val videoProjection = projection + arrayOf(
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        // Query Images
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(
            imageUri,
            imageProjection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateAddCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val dateTakenCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val orientCol = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val bucketIdCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(imageUri, id)
                val name = cursor.getString(nameCol) ?: ""
                val path = cursor.getString(pathCol) ?: ""
                val mime = cursor.getString(mimeCol) ?: "image/jpeg"
                val size = cursor.getLong(sizeCol)
                val dateAdded = cursor.getLong(dateAddCol)
                val dateModified = cursor.getLong(dateModCol)
                val dateTaken = if (dateTakenCol != -1) cursor.getLong(dateTakenCol) else 0L
                val orient = if (orientCol != -1) cursor.getInt(orientCol) else 0
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)
                val bucketId = if (bucketIdCol != -1) cursor.getString(bucketIdCol) ?: "" else ""
                val bucketName = if (bucketNameCol != -1) cursor.getString(bucketNameCol) ?: "" else ""

                items.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        path = path,
                        displayName = name,
                        mimeType = mime,
                        size = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        dateTaken = dateTaken,
                        durationMs = 0L,
                        width = width,
                        height = height,
                        isVideo = false,
                        bucketId = bucketId,
                        bucketDisplayName = bucketName,
                        orientation = orient
                    )
                )
            }
        }

        // Query Videos
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(
            videoUri,
            videoProjection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateAddCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val dateTakenCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
            val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val bucketIdCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(videoUri, id)
                val name = cursor.getString(nameCol) ?: ""
                val path = cursor.getString(pathCol) ?: ""
                val mime = cursor.getString(mimeCol) ?: "video/mp4"
                val size = cursor.getLong(sizeCol)
                val dateAdded = cursor.getLong(dateAddCol)
                val dateModified = cursor.getLong(dateModCol)
                val dateTaken = if (dateTakenCol != -1) cursor.getLong(dateTakenCol) else 0L
                val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)
                val bucketId = if (bucketIdCol != -1) cursor.getString(bucketIdCol) ?: "" else ""
                val bucketName = if (bucketNameCol != -1) cursor.getString(bucketNameCol) ?: "" else ""

                items.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        path = path,
                        displayName = name,
                        mimeType = mime,
                        size = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        dateTaken = dateTaken,
                        durationMs = duration,
                        width = width,
                        height = height,
                        isVideo = true,
                        bucketId = bucketId,
                        bucketDisplayName = bucketName,
                        orientation = 0
                    )
                )
            }
        }

        // Sort by best timestamp descending
        items.sortByDescending { it.bestTimestamp }
        return items
    }

    /**
     * Extracts distinct device folders (albums) from MediaStore items.
     */
    fun extractDeviceAlbums(mediaList: List<MediaItem>): List<Album> {
        val groups = mediaList.groupBy { it.bucketId }
        val albums = mutableListOf<Album>()

        for ((bucketId, items) in groups) {
            if (bucketId.isBlank() || items.isEmpty()) continue
            val first = items.first()
            val folderName = first.bucketDisplayName.ifBlank { "Folder" }

            val type = when (folderName.lowercase()) {
                "camera", "dcim" -> AlbumType.CAMERA
                "screenshots" -> AlbumType.SCREENSHOTS
                "download", "downloads" -> AlbumType.DOWNLOADS
                "movies", "video", "videos" -> AlbumType.VIDEOS
                else -> AlbumType.DEVICE_FOLDER
            }

            albums.add(
                Album(
                    id = bucketId,
                    name = folderName,
                    coverUri = first.uri,
                    itemCount = items.size,
                    type = type,
                    bucketId = bucketId
                )
            )
        }

        // Sort: Camera first, Screenshots second, Downloads third, then alphabetical
        return albums.sortedWith(
            compareBy<Album> {
                when (it.type) {
                    AlbumType.CAMERA -> 0
                    AlbumType.SCREENSHOTS -> 1
                    AlbumType.DOWNLOADS -> 2
                    AlbumType.VIDEOS -> 3
                    else -> 4
                }
            }.thenBy { it.name.lowercase() }
        )
    }

    /**
     * Queries MediaStore trashed items on Android 11+ (API 30+).
     */
    suspend fun queryPublicTrashedMedia(): List<TrashedMediaItem> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return@withContext emptyList()
        }

        val trashed = mutableListOf<TrashedMediaItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_EXPIRES
        )

        val bundle = android.os.Bundle().apply {
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
        }

        try {
            // Check images
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                bundle,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val expiresCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_EXPIRES)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    val name = cursor.getString(nameCol) ?: ""
                    val mime = cursor.getString(mimeCol) ?: "image/jpeg"
                    val size = cursor.getLong(sizeCol)
                    val expires = if (expiresCol != -1) cursor.getLong(expiresCol) * 1000L else 0L

                    trashed.add(
                        TrashedMediaItem(
                            id = id,
                            uri = uri,
                            displayName = name,
                            mimeType = mime,
                            size = size,
                            isVideo = false,
                            trashedTimeMs = if (expires > 0) expires - (30L * 24 * 60 * 60 * 1000L) else System.currentTimeMillis()
                        )
                    )
                }
            }

            // Check videos
            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                bundle,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val expiresCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_EXPIRES)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val name = cursor.getString(nameCol) ?: ""
                    val mime = cursor.getString(mimeCol) ?: "video/mp4"
                    val size = cursor.getLong(sizeCol)
                    val expires = if (expiresCol != -1) cursor.getLong(expiresCol) * 1000L else 0L

                    trashed.add(
                        TrashedMediaItem(
                            id = id,
                            uri = uri,
                            displayName = name,
                            mimeType = mime,
                            size = size,
                            isVideo = true,
                            trashedTimeMs = if (expires > 0) expires - (30L * 24 * 60 * 60 * 1000L) else System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        trashed.sortedByDescending { it.trashedTimeMs }
    }
}

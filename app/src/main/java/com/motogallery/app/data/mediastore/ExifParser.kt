package com.motogallery.app.data.mediastore

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.MediaMetadata
import com.motogallery.app.util.DateFormatter
import com.motogallery.app.util.FileSizeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExifParser {

    suspend fun parseMetadata(context: Context, item: MediaItem): MediaMetadata = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        if (item.isVideo) {
            parseVideoMetadata(context, item)
        } else {
            parseImageMetadata(context, item)
        }
    }

    private fun parseImageMetadata(context: Context, item: MediaItem): MediaMetadata {
        var make: String? = null
        var model: String? = null
        var lens: String? = null
        var aperture: String? = null
        var shutterSpeed: String? = null
        var iso: String? = null
        var focalLength: String? = null
        var flash: String? = null
        var whiteBalance: String? = null
        var lat: Double? = null
        var lon: Double? = null
        var width = item.width
        var height = item.height
        var dateTakenFormatted = DateFormatter.formatFullDateTime(item.bestTimestamp)

        try {
            context.contentResolver.openInputStream(item.uri)?.use { stream ->
                val exif = ExifInterface(stream)

                make = exif.getAttribute(ExifInterface.TAG_MAKE)
                model = exif.getAttribute(ExifInterface.TAG_MODEL)
                lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)

                exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let {
                    aperture = "f/$it"
                }

                exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let {
                    val exp = it.toDoubleOrNull()
                    if (exp != null) {
                        shutterSpeed = if (exp < 1.0 && exp > 0) {
                            "1/${(1.0 / exp).toInt()}s"
                        } else {
                            "${exp}s"
                        }
                    } else {
                        shutterSpeed = it
                    }
                }

                iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                    ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)

                exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let {
                    focalLength = "${it}mm"
                }

                val flashVal = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                if (flashVal != -1) {
                    flash = if (flashVal and 1 != 0) "Fired" else "Did not fire"
                }

                val wbVal = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1)
                if (wbVal != -1) {
                    whiteBalance = if (wbVal == ExifInterface.WHITE_BALANCE_AUTO.toInt()) "Auto" else "Manual"
                }

                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    lat = latLong[0].toDouble()
                    lon = latLong[1].toDouble()
                }

                val exifW = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                val exifH = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                if (exifW > 0 && exifH > 0) {
                    width = exifW
                    height = exifH
                }
            }
        } catch (_: Exception) {}

        return MediaMetadata(
            title = item.displayName,
            path = item.path,
            sizeBytes = item.size,
            mimeType = item.mimeType,
            width = width,
            height = height,
            dateTakenFormatted = dateTakenFormatted,
            cameraMake = make,
            cameraModel = model,
            lensModel = lens,
            aperture = aperture,
            shutterSpeed = shutterSpeed,
            iso = iso,
            focalLength = focalLength,
            flash = flash,
            whiteBalance = whiteBalance,
            latitude = lat,
            longitude = lon
        )
    }

    private fun parseVideoMetadata(context: Context, item: MediaItem): MediaMetadata {
        var width = item.width
        var height = item.height
        var durationMs = item.durationMs
        var bitrate: String? = null
        var lat: Double? = null
        var lon: Double? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, item.uri)

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()?.let {
                width = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()?.let {
                height = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                durationMs = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()?.let {
                bitrate = "${it / 1000} kbps"
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)?.let { loc ->
                // Example format "+37.5090+127.0628/"
                try {
                    val regex = Regex("([+-][0-9.]+)([+-][0-9.]+)")
                    val match = regex.find(loc)
                    if (match != null) {
                        lat = match.groupValues[1].toDoubleOrNull()
                        lon = match.groupValues[2].toDoubleOrNull()
                    }
                } catch (_: Exception) {}
            }

            retriever.release()
        } catch (_: Exception) {}

        return MediaMetadata(
            title = item.displayName,
            path = item.path,
            sizeBytes = item.size,
            mimeType = item.mimeType,
            width = width,
            height = height,
            dateTakenFormatted = DateFormatter.formatFullDateTime(item.bestTimestamp),
            durationFormatted = DateFormatter.formatDuration(durationMs),
            bitrate = bitrate,
            latitude = lat,
            longitude = lon
        )
    }
}

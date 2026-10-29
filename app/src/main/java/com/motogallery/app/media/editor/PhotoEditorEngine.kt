package com.motogallery.app.media.editor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoEditState(
    val rotationAngle: Float = 0f,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val straightenAngle: Float = 0f,
    val cropAspectRatio: Float? = null, // null = original, 1f, 4/3f, 16/9f, 9/16f
    val brightness: Float = 0f, // -100 to 100
    val contrast: Float = 0f, // -100 to 100
    val saturation: Float = 0f, // -100 to 100
    val warmth: Float = 0f, // -100 to 100
    val shadowsHighlights: Float = 0f, // -100 to 100
    val filter: PhotoFilter = PhotoFilter.ORIGINAL
) {
    val isModified: Boolean
        get() = rotationAngle != 0f ||
                isFlippedHorizontal ||
                isFlippedVertical ||
                straightenAngle != 0f ||
                cropAspectRatio != null ||
                brightness != 0f ||
                contrast != 0f ||
                saturation != 0f ||
                warmth != 0f ||
                shadowsHighlights != 0f ||
                filter != PhotoFilter.ORIGINAL
}

class PhotoEditorEngine(private val context: Context) {

    /**
     * Loads a downsampled preview bitmap for fast, responsive interactive editing in Compose.
     */
    suspend fun loadPreviewBitmap(uri: Uri, maxDimension: Int = 1080): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var sampleSize = 1
            val maxOut = maxOf(options.outWidth, options.outHeight)
            while (maxOut / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads the full-resolution source bitmap for high-quality final export.
     */
    suspend fun loadFullBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Applies full editing pipeline (transformations, crop, adjustments, filter) to the input bitmap.
     */
    fun processBitmap(source: Bitmap, state: PhotoEditState): Bitmap {
        // Step 1: Geometric Transformations (Rotate, Flip, Straighten)
        val matrix = Matrix()
        val totalRotation = (state.rotationAngle + state.straightenAngle) % 360f
        matrix.postRotate(totalRotation, source.width / 2f, source.height / 2f)

        val sx = if (state.isFlippedHorizontal) -1f else 1f
        val sy = if (state.isFlippedVertical) -1f else 1f
        if (sx != 1f || sy != 1f) {
            matrix.postScale(sx, sy, source.width / 2f, source.height / 2f)
        }

        var transformed = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )

        // Step 2: Aspect Ratio Crop if specified
        state.cropAspectRatio?.let { targetRatio ->
            val curRatio = transformed.width.toFloat() / transformed.height.toFloat()
            val cropW: Int
            val cropH: Int
            val cropX: Int
            val cropY: Int

            if (curRatio > targetRatio) {
                cropH = transformed.height
                cropW = (cropH * targetRatio).toInt().coerceAtMost(transformed.width)
                cropX = (transformed.width - cropW) / 2
                cropY = 0
            } else {
                cropW = transformed.width
                cropH = (cropW / targetRatio).toInt().coerceAtMost(transformed.height)
                cropX = 0
                cropY = (transformed.height - cropH) / 2
            }

            if (cropW > 0 && cropH > 0 && (cropW != transformed.width || cropH != transformed.height)) {
                val cropped = Bitmap.createBitmap(transformed, cropX, cropY, cropW, cropH)
                if (transformed != source) transformed.recycle()
                transformed = cropped
            }
        }

        // Step 3: Color Adjustments & Preset Filter
        val finalMatrix = ColorMatrix()

        // Apply Preset Filter first
        finalMatrix.postConcat(state.filter.getColorMatrix())

        // Apply Brightness (-100 to 100)
        if (state.brightness != 0f) {
            val bShift = state.brightness * 1.5f
            val bMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, bShift,
                    0f, 1f, 0f, 0f, bShift,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            finalMatrix.postConcat(bMatrix)
        }

        // Apply Contrast (-100 to 100)
        if (state.contrast != 0f) {
            val scale = (state.contrast + 100f) / 100f
            val translate = (-0.5f * scale + 0.5f) * 255f
            val cMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            finalMatrix.postConcat(cMatrix)
        }

        // Apply Saturation (-100 to 100)
        if (state.saturation != 0f) {
            val satVal = (state.saturation + 100f) / 100f
            val sMatrix = ColorMatrix().apply { setSaturation(satVal.coerceAtLeast(0f)) }
            finalMatrix.postConcat(sMatrix)
        }

        // Apply Warmth (Color Temperature shift: red up/blue down or vice versa)
        if (state.warmth != 0f) {
            val rShift = state.warmth * 0.75f
            val bShift = -state.warmth * 0.75f
            val wMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, rShift,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            finalMatrix.postConcat(wMatrix)
        }

        // Apply Shadows & Highlights (-100 to 100)
        if (state.shadowsHighlights != 0f) {
            val shShift = state.shadowsHighlights * 0.5f
            val shMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, shShift,
                    0f, 1f, 0f, 0f, shShift,
                    0f, 0f, 1f, 0f, shShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            finalMatrix.postConcat(shMatrix)
        }

        // Render adjusted bitmap
        val result = Bitmap.createBitmap(
            transformed.width,
            transformed.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(finalMatrix)
        }
        canvas.drawBitmap(transformed, 0f, 0f, paint)

        if (transformed != source) {
            transformed.recycle()
        }

        return result
    }

    /**
     * Non-destructively exports the edited image as a NEW copy in MediaStore Pictures/MotoGallery.
     */
    suspend fun exportEditedImage(
        originalUri: Uri,
        state: PhotoEditState,
        quality: Int = 95
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fullBitmap = loadFullBitmap(originalUri)
                ?: return@withContext Result.failure(Exception("Failed to decode source image at full resolution"))

            val processed = processBitmap(fullBitmap, state)
            fullBitmap.recycle()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val displayName = "Moto_Edit_${timestamp}.jpg"
            val relativePath = "${Environment.DIRECTORY_PICTURES}/MotoGallery"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val targetUri = context.contentResolver.insert(collection, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry for export"))

            context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                processed.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            processed.recycle()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(targetUri, contentValues, null, null)
            }

            Result.success(targetUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.motogallery.app.media.editor

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoEditorEngine(private val context: Context) {

    /**
     * Trims and optionally mutes a video using hardware-accelerated MediaExtractor & MediaMuxer.
     * Fast, zero re-encode degradation, low memory footprint.
     */
    suspend fun trimAndExportVideo(
        sourceUri: Uri,
        startMs: Long,
        endMs: Long,
        muteAudio: Boolean,
        onProgress: (Float) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var tempFile: File? = null

        try {
            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            val durationUs = (endUs - startUs).coerceAtLeast(1L)

            // Step 1: Prepare temp file for muxing output
            val cacheDir = context.cacheDir
            tempFile = File.createTempFile("moto_trim_", ".mp4", cacheDir)

            extractor = MediaExtractor()
            extractor.setDataSource(context, sourceUri, null)

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // Step 2: Query video orientation to preserve it
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, sourceUri)
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                muxer.setOrientationHint(rotation)
                retriever.release()
            } catch (_: Exception) {}

            val trackCount = extractor.trackCount
            val trackMap = mutableMapOf<Int, Int>() // extractorTrackIndex -> muxerTrackIndex
            var videoTrackIndex = -1

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                if (mime.startsWith("video/")) {
                    videoTrackIndex = i
                    val muxerIndex = muxer.addTrack(format)
                    trackMap[i] = muxerIndex
                } else if (mime.startsWith("audio/") && !muteAudio) {
                    val muxerIndex = muxer.addTrack(format)
                    trackMap[i] = muxerIndex
                }
            }

            if (videoTrackIndex == -1) {
                return@withContext Result.failure(Exception("No video track found in source media"))
            }

            muxer.start()

            // Step 3: Seek extractor to start time
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            // Select active tracks
            for (trackIndex in trackMap.keys) {
                extractor.selectTrack(trackIndex)
            }

            val maxBufferSize = 1024 * 1024 // 1 MB buffer
            val buffer = ByteBuffer.allocateDirect(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            var ptsOffsetUs = -1L

            while (isActive) {
                val trackIndex = extractor.sampleTrackIndex
                if (trackIndex < 0) break // End of stream

                val muxerTrack = trackMap[trackIndex]
                if (muxerTrack != null) {
                    buffer.clear()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    val sampleTime = extractor.sampleTime

                    if (sampleTime > endUs) {
                        // Reached trim end
                        break
                    }

                    if (sampleTime >= startUs) {
                        if (ptsOffsetUs < 0) {
                            ptsOffsetUs = sampleTime
                        }

                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = (sampleTime - ptsOffsetUs).coerceAtLeast(0L)
                        val sampleFlags = extractor.sampleFlags
                        bufferInfo.flags = if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                            MediaCodec.BUFFER_FLAG_KEY_FRAME
                        } else 0

                        muxer.writeSampleData(muxerTrack, buffer, bufferInfo)

                        if (trackIndex == videoTrackIndex) {
                            val progress = ((sampleTime - startUs).toFloat() / durationUs.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                }
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            muxer = null

            extractor.release()
            extractor = null

            if (!isActive) {
                tempFile.delete()
                return@withContext Result.failure(Exception("Video export cancelled"))
            }

            onProgress(1.0f)

            // Step 4: Move completed file to MediaStore Movies/MotoGallery
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val displayName = "Moto_Trim_${timestamp}.mp4"
            val relativePath = "${Environment.DIRECTORY_MOVIES}/MotoGallery"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val targetUri = context.contentResolver.insert(collection, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry for video"))

            context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                FileInputStream(tempFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            } ?: return@withContext Result.failure(Exception("Failed to stream video to MediaStore"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(targetUri, contentValues, null, null)
            }

            tempFile.delete()
            Result.success(targetUri)
        } catch (e: Exception) {
            try { muxer?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
            tempFile?.delete()
            Result.failure(e)
        }
    }
}

package com.motogallery.app.ui.screens.editor

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.media.editor.VideoEditorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VideoEditorUiState(
    val durationMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val isMuted: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val engine = VideoEditorEngine(app)

    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private var currentSourceUri: Uri? = null
    private var exportJob: Job? = null

    fun loadSource(uri: Uri) {
        currentSourceUri = uri
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(app, uri)
                    val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    retriever.release()

                    _uiState.value = _uiState.value.copy(
                        durationMs = dur,
                        trimStartMs = 0L,
                        trimEndMs = dur
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to load video: ${e.message}")
                }
            }
        }
    }

    fun setTrimRange(startMs: Long, endMs: Long) {
        val total = _uiState.value.durationMs
        val clampedStart = startMs.coerceIn(0L, total)
        val clampedEnd = endMs.coerceIn(clampedStart, total)
        _uiState.value = _uiState.value.copy(
            trimStartMs = clampedStart,
            trimEndMs = clampedEnd
        )
    }

    fun toggleMuteAudio() {
        _uiState.value = _uiState.value.copy(isMuted = !_uiState.value.isMuted)
    }

    fun exportTrimmedVideo(onSuccess: (Uri) -> Unit) {
        val uri = currentSourceUri ?: return
        exportJob?.cancel()

        exportJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportProgress = 0f)

            val res = engine.trimAndExportVideo(
                sourceUri = uri,
                startMs = _uiState.value.trimStartMs,
                endMs = _uiState.value.trimEndMs,
                muteAudio = _uiState.value.isMuted,
                onProgress = { p ->
                    _uiState.value = _uiState.value.copy(exportProgress = p)
                }
            )

            _uiState.value = _uiState.value.copy(isExporting = false)

            if (res.isSuccess) {
                val exported = res.getOrThrow()
                _uiState.value = _uiState.value.copy(exportSuccessUri = exported)
                onSuccess(exported)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Export failed: ${res.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 0f)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, exportSuccessUri = null)
    }
}

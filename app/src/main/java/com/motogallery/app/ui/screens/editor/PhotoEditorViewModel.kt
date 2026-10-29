package com.motogallery.app.ui.screens.editor

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.media.editor.PhotoEditState
import com.motogallery.app.media.editor.PhotoEditorEngine
import com.motogallery.app.media.editor.PhotoFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhotoEditorUiState(
    val currentState: PhotoEditState = PhotoEditState(),
    val previewBitmap: Bitmap? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activeTab: EditorTab = EditorTab.ADJUST,
    val exportSuccessUri: Uri? = null,
    val errorMessage: String? = null
)

enum class EditorTab {
    CROP,
    ADJUST,
    FILTERS
}

class PhotoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val engine = PhotoEditorEngine(app)

    private val _uiState = MutableStateFlow(PhotoEditorUiState())
    val uiState: StateFlow<PhotoEditorUiState> = _uiState.asStateFlow()

    private var rawPreviewBitmap: Bitmap? = null
    private var sourceUri: Uri? = null

    private val undoStack = mutableListOf<PhotoEditState>()
    private val redoStack = mutableListOf<PhotoEditState>()

    private var renderJob: Job? = null

    fun loadSource(uri: Uri) {
        sourceUri = uri
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rawPreviewBitmap = engine.loadPreviewBitmap(uri, maxDimension = 1080)
            _uiState.value = _uiState.value.copy(
                previewBitmap = rawPreviewBitmap,
                isLoading = false
            )
        }
    }

    fun setActiveTab(tab: EditorTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun updateState(transform: (PhotoEditState) -> PhotoEditState) {
        val old = _uiState.value.currentState
        undoStack.add(old)
        redoStack.clear()

        val next = transform(old)
        _uiState.value = _uiState.value.copy(
            currentState = next,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
        scheduleRender()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.currentState
        redoStack.add(current)
        val prev = undoStack.removeAt(undoStack.size - 1)
        _uiState.value = _uiState.value.copy(
            currentState = prev,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
        scheduleRender()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.currentState
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.size - 1)
        _uiState.value = _uiState.value.copy(
            currentState = next,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
        scheduleRender()
    }

    fun reset() {
        updateState { PhotoEditState() }
    }

    fun rotate90() {
        updateState { it.copy(rotationAngle = (it.rotationAngle + 90f) % 360f) }
    }

    fun flipHorizontal() {
        updateState { it.copy(isFlippedHorizontal = !it.isFlippedHorizontal) }
    }

    fun flipVertical() {
        updateState { it.copy(isFlippedVertical = !it.isFlippedVertical) }
    }

    fun setStraighten(angle: Float) {
        updateState { it.copy(straightenAngle = angle) }
    }

    fun setCropAspectRatio(ratio: Float?) {
        updateState { it.copy(cropAspectRatio = ratio) }
    }

    fun setBrightness(value: Float) {
        updateState { it.copy(brightness = value) }
    }

    fun setContrast(value: Float) {
        updateState { it.copy(contrast = value) }
    }

    fun setSaturation(value: Float) {
        updateState { it.copy(saturation = value) }
    }

    fun setWarmth(value: Float) {
        updateState { it.copy(warmth = value) }
    }

    fun setShadowsHighlights(value: Float) {
        updateState { it.copy(shadowsHighlights = value) }
    }

    fun setFilter(filter: PhotoFilter) {
        updateState { it.copy(filter = filter) }
    }

    private fun scheduleRender() {
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            val source = rawPreviewBitmap ?: return@launch
            val state = _uiState.value.currentState
            val processed = withContext(Dispatchers.Default) {
                engine.processBitmap(source, state)
            }
            _uiState.value = _uiState.value.copy(previewBitmap = processed)
        }
    }

    fun exportCopy(onSuccess: (Uri) -> Unit) {
        val uri = sourceUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val res = engine.exportEditedImage(uri, _uiState.value.currentState)
            _uiState.value = _uiState.value.copy(isExporting = false)
            if (res.isSuccess) {
                val exportedUri = res.getOrThrow()
                _uiState.value = _uiState.value.copy(exportSuccessUri = exportedUri)
                onSuccess(exportedUri)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Export failed: ${res.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, exportSuccessUri = null)
    }
}

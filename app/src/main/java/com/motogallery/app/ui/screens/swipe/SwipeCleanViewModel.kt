package com.motogallery.app.ui.screens.swipe

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.SwipeActionRecord
import com.motogallery.app.data.model.SwipeCategory
import com.motogallery.app.data.model.SwipeDirection
import com.motogallery.app.data.model.SwipeMinSize
import com.motogallery.app.data.model.SwipeSort
import com.motogallery.app.util.FileSizeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SwipeCleanUiState(
    val category: SwipeCategory = SwipeCategory.ALL_PHOTOS,
    val sort: SwipeSort = SwipeSort.DATE_DESC,
    val minSize: SwipeMinSize = SwipeMinSize.ALL,
    val unreviewedItems: List<MediaItem> = emptyList(),
    val reviewedKept: List<MediaItem> = emptyList(),
    val reviewedTrashQueue: List<MediaItem> = emptyList(),
    val canUndo: Boolean = false,
    val totalOriginalCount: Int = 0,
    val recoverableBytes: Long = 0L,
    val isLoading: Boolean = false,
    val previewMediaItem: MediaItem? = null,
    val showTrashConfirmDialog: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class SwipeCleanViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaStoreRepository(application)
    private val operationsHelper = MediaOperationsHelper(application)

    private val _uiState = MutableStateFlow(SwipeCleanUiState(isLoading = true))
    val uiState: StateFlow<SwipeCleanUiState> = _uiState.asStateFlow()

    // Undo History Stack
    private val actionHistory = mutableListOf<SwipeActionRecord>()

    // All cached raw items for the current category before swiping
    private var allCategoryItems: List<MediaItem> = emptyList()

    init {
        loadCategoryMedia(SwipeCategory.ALL_PHOTOS)
    }

    fun loadCategoryMedia(category: SwipeCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, category = category)
            try {
                // Fetch public media from MediaStore (never include private vault items!)
                val allMedia: List<MediaItem> = mediaRepository.queryAllMedia()

                // Filter by Category
                val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
                val filtered = allMedia.filter { item: MediaItem ->
                    when (category) {
                        SwipeCategory.ALL_PHOTOS -> !item.isVideo
                        SwipeCategory.ALL_VIDEOS -> item.isVideo
                        SwipeCategory.SCREENSHOTS -> {
                            item.displayName.contains("screenshot", ignoreCase = true) ||
                                    item.bucketDisplayName.contains("screenshot", ignoreCase = true)
                        }
                        SwipeCategory.LARGE_FILES -> item.size >= 25L * 1024 * 1024
                        SwipeCategory.OLD_MEDIA -> item.bestTimestamp < sixMonthsAgo
                        SwipeCategory.DOWNLOADS -> {
                            item.bucketDisplayName.contains("download", ignoreCase = true) ||
                                    item.displayName.contains("download", ignoreCase = true)
                        }
                        SwipeCategory.CAMERA -> {
                            item.bucketDisplayName.contains("camera", ignoreCase = true) ||
                                    item.bucketDisplayName.contains("dcim", ignoreCase = true)
                        }
                    }
                }

                allCategoryItems = filtered
                actionHistory.clear()
                applySortAndFilters(filtered, _uiState.value.sort, _uiState.value.minSize)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load media for review: ${e.message}"
                )
            }
        }
    }

    private fun applySortAndFilters(
        items: List<MediaItem>,
        sort: SwipeSort,
        minSize: SwipeMinSize
    ) {
        val sizeFiltered = if (minSize.minBytes > 0) {
            items.filter { it.size >= minSize.minBytes }
        } else items

        val sorted = when (sort) {
            SwipeSort.DATE_DESC -> sizeFiltered.sortedByDescending { it.bestTimestamp }
            SwipeSort.DATE_ASC -> sizeFiltered.sortedBy { it.bestTimestamp }
            SwipeSort.SIZE_DESC -> sizeFiltered.sortedByDescending { it.size }
            SwipeSort.SIZE_ASC -> sizeFiltered.sortedBy { it.size }
        }

        _uiState.value = _uiState.value.copy(
            unreviewedItems = sorted,
            reviewedKept = emptyList(),
            reviewedTrashQueue = emptyList(),
            canUndo = false,
            totalOriginalCount = sorted.size,
            recoverableBytes = 0L,
            isLoading = false
        )
    }

    fun setSort(sort: SwipeSort) {
        if (_uiState.value.sort == sort) return
        _uiState.value = _uiState.value.copy(sort = sort)
        applySortAndFilters(allCategoryItems, sort, _uiState.value.minSize)
    }

    fun setMinSize(minSize: SwipeMinSize) {
        if (_uiState.value.minSize == minSize) return
        _uiState.value = _uiState.value.copy(minSize = minSize)
        applySortAndFilters(allCategoryItems, _uiState.value.sort, minSize)
    }

    fun swipeRight(item: MediaItem) {
        // KEEP action
        val currentUnreviewed = _uiState.value.unreviewedItems.toMutableList()
        if (currentUnreviewed.remove(item)) {
            val updatedKept = _uiState.value.reviewedKept + item
            actionHistory.add(SwipeActionRecord(item, SwipeDirection.RIGHT))
            _uiState.value = _uiState.value.copy(
                unreviewedItems = currentUnreviewed,
                reviewedKept = updatedKept,
                canUndo = true
            )
        }
    }

    fun swipeLeft(item: MediaItem) {
        // TRASH action (queues item for trash, does not delete immediately)
        val currentUnreviewed = _uiState.value.unreviewedItems.toMutableList()
        if (currentUnreviewed.remove(item)) {
            val updatedTrash = _uiState.value.reviewedTrashQueue + item
            actionHistory.add(SwipeActionRecord(item, SwipeDirection.LEFT))
            val newRecoverable = updatedTrash.sumOf { it.size }
            _uiState.value = _uiState.value.copy(
                unreviewedItems = currentUnreviewed,
                reviewedTrashQueue = updatedTrash,
                recoverableBytes = newRecoverable,
                canUndo = true
            )
        }
    }

    fun swipeUp(item: MediaItem) {
        // PREVIEW action
        _uiState.value = _uiState.value.copy(previewMediaItem = item)
    }

    fun undo() {
        if (actionHistory.isEmpty()) return
        val lastAction = actionHistory.removeAt(actionHistory.lastIndex)
        val item = lastAction.mediaItem

        val currentUnreviewed = (_uiState.value.unreviewedItems.toMutableList()).apply {
            add(0, item) // Restore to top of stack
        }

        val updatedKept = _uiState.value.reviewedKept.filterNot { it.id == item.id }
        val updatedTrash = _uiState.value.reviewedTrashQueue.filterNot { it.id == item.id }
        val newRecoverable = updatedTrash.sumOf { it.size }

        _uiState.value = _uiState.value.copy(
            unreviewedItems = currentUnreviewed,
            reviewedKept = updatedKept,
            reviewedTrashQueue = updatedTrash,
            recoverableBytes = newRecoverable,
            canUndo = actionHistory.isNotEmpty()
        )
    }

    fun setPreviewMediaItem(item: MediaItem?) {
        _uiState.value = _uiState.value.copy(previewMediaItem = item)
    }

    fun setShowTrashConfirmDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTrashConfirmDialog = show)
    }

    fun executeTrashQueue(onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit) {
        val trashList = _uiState.value.reviewedTrashQueue
        if (trashList.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showTrashConfirmDialog = false)
            val uris = trashList.map { it.uri }
            when (val result = operationsHelper.requestTrashMedia(uris, trash = true)) {
                is MediaOperationResult.RequiresConsent -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onRequestConsent(result.intentSender) { granted ->
                        onConsentResult(granted)
                    }
                }
                is MediaOperationResult.Success -> {
                    onTrashSuccess(trashList)
                }
                is MediaOperationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not move to trash: ${result.message}"
                    )
                }
            }
        }
    }

    fun onConsentResult(granted: Boolean) {
        if (granted) {
            onTrashSuccess(_uiState.value.reviewedTrashQueue)
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Trash permission was denied by user"
            )
        }
    }

    private fun onTrashSuccess(trashed: List<MediaItem>) {
        val count = trashed.size
        val sizeFormatted = FileSizeFormatter.format(trashed.sumOf { it.size })
        actionHistory.clear()
        _uiState.value = _uiState.value.copy(
            reviewedTrashQueue = emptyList(),
            recoverableBytes = 0L,
            canUndo = false,
            isLoading = false,
            statusMessage = "Moved $count items ($sizeFormatted) to Recently Deleted"
        )
    }

    fun resetSession() {
        actionHistory.clear()
        applySortAndFilters(allCategoryItems, _uiState.value.sort, _uiState.value.minSize)
        _uiState.value = _uiState.value.copy(statusMessage = "Reset review session")
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

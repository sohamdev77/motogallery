package com.motogallery.app.ui.screens.videos

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.local.entity.FavoriteEntity
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.SortBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class VideosUiState(
    val videos: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
    val sortBy: SortBy = SortBy.DATE_DESC,
    val searchQuery: String = ""
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectedItems: List<MediaItem> get() = videos.filter { it.id in selectedIds }
}

class VideosViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val repository = MediaStoreRepository(app)
    private val favoriteDao = app.database.favoriteDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.DATE_DESC)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val uiState: StateFlow<VideosUiState> = combine(
        repository.observeMedia(),
        favoriteDao.getAllFavoriteIdsFlow(),
        _sortBy,
        _searchQuery,
        _selectedIds
    ) { mediaList, favIds, sortBy, query, selected ->
        val favSet = favIds.toSet()
        var vids = mediaList.filter { it.isVideo }.map { it.copy(isFavorite = it.id in favSet) }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            vids = vids.filter { it.displayName.lowercase().contains(q) }
        }

        vids = when (sortBy) {
            SortBy.DATE_DESC -> vids.sortedByDescending { it.bestTimestamp }
            SortBy.DATE_ASC -> vids.sortedBy { it.bestTimestamp }
            SortBy.NAME_ASC -> vids.sortedBy { it.displayName.lowercase() }
            SortBy.NAME_DESC -> vids.sortedByDescending { it.displayName.lowercase() }
            SortBy.SIZE_DESC -> vids.sortedByDescending { it.size }
            SortBy.SIZE_ASC -> vids.sortedBy { it.size }
        }

        VideosUiState(
            videos = vids,
            isLoading = false,
            selectedIds = selected,
            sortBy = sortBy,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VideosUiState()
    )

    private val operationsHelper = MediaOperationsHelper(app)
    private val vaultManager = app.vaultManager

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (id in current) current.remove(id) else current.add(id)
        _selectedIds.value = current
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleFavoriteSelected() {
        viewModelScope.launch {
            val selected = uiState.value.selectedItems
            val allFav = selected.all { it.isFavorite }
            if (allFav) {
                favoriteDao.deleteFavorites(selected.map { it.id })
            } else {
                for (item in selected) {
                    favoriteDao.insertFavorite(
                        FavoriteEntity(mediaId = item.id, uriString = item.uri.toString())
                    )
                }
            }
            clearSelection()
        }
    }

    fun requestDeleteSelected(
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val selected = uiState.value.selectedItems
            val uris = selected.map { it.uri }
            when (val res = operationsHelper.requestTrashMedia(uris, trash = true)) {
                is MediaOperationResult.RequiresConsent -> {
                    onRequestConsent(res.intentSender) { granted ->
                        if (granted) {
                            clearSelection()
                            onComplete()
                        }
                    }
                }
                is MediaOperationResult.Success -> {
                    clearSelection()
                    onComplete()
                }
                is MediaOperationResult.Error -> {
                    _errorMessage.value = res.message
                }
            }
        }
    }

    fun moveToVault(
        items: List<MediaItem>,
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
        onSuccess: (importedCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            var importedCount = 0
            val successfulOriginalUris = mutableListOf<Uri>()
            val successfulOriginalPaths = mutableListOf<String>()

            for (item in items) {
                val res = vaultManager.importMedia(
                    sourceUri = item.uri,
                    originalName = item.displayName,
                    mimeType = item.mimeType,
                    isVideo = item.isVideo,
                    size = item.size,
                    durationMs = item.durationMs,
                    width = item.width,
                    height = item.height
                )
                if (res.isSuccess) {
                    importedCount++
                    successfulOriginalUris.add(item.uri)
                    if (item.path.isNotBlank()) successfulOriginalPaths.add(item.path)
                }
            }

            clearSelection()

            if (successfulOriginalUris.isNotEmpty()) {
                onSuccess(importedCount)
                // Request user consent to delete public originals so they are only visible in vault!
                when (val delRes = operationsHelper.requestDeleteMedia(successfulOriginalUris, successfulOriginalPaths)) {
                    is MediaOperationResult.RequiresConsent -> {
                        onRequestConsent(delRes.intentSender) { granted ->
                            if (granted) {
                                successfulOriginalPaths.forEach { File(it).delete() }
                                viewModelScope.launch {
                                    favoriteDao.deleteFavorites(items.map { it.id })
                                }
                            }
                        }
                    }
                    is MediaOperationResult.Success -> {
                        successfulOriginalPaths.forEach { File(it).delete() }
                        favoriteDao.deleteFavorites(items.map { it.id })
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

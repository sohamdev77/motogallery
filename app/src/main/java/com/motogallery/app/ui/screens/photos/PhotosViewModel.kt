package com.motogallery.app.ui.screens.photos

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
import com.motogallery.app.data.model.FilterSortOptions
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.MediaTypeFilter
import com.motogallery.app.data.model.SortBy
import com.motogallery.app.data.preferences.AppPreferences
import com.motogallery.app.data.vault.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PhotosUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
    val gridColumns: Int = 3,
    val sortBy: SortBy = SortBy.DATE_DESC,
    val mediaTypeFilter: MediaTypeFilter = MediaTypeFilter.ALL,
    val searchQuery: String = "",
    val error: String? = null
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectedItems: List<MediaItem> get() = mediaItems.filter { it.id in selectedIds }
}

class PhotosViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val repository = MediaStoreRepository(app)
    private val favoriteDao = app.database.favoriteDao()
    private val customAlbumDao = app.database.customAlbumDao()
    private val operationsHelper = MediaOperationsHelper(app)
    private val preferences = app.preferences
    private val vaultManager = app.vaultManager

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private data class FilterConfig(
        val sortBy: SortBy,
        val filter: MediaTypeFilter,
        val query: String,
        val columns: Int
    )

    private val filterConfigFlow = combine(
        preferences.sortBy,
        preferences.mediaTypeFilter,
        _searchQuery,
        preferences.gridColumns
    ) { sortBy, filter, query, cols ->
        FilterConfig(sortBy, filter, query, cols)
    }

    private val mediaWithFavoritesFlow = combine(
        repository.observeMedia(),
        favoriteDao.getAllFavoriteIdsFlow()
    ) { mediaList, favoriteIds ->
        val favoriteIdSet = favoriteIds.toSet()
        mediaList.map { item ->
            item.copy(isFavorite = item.id in favoriteIdSet)
        }
    }

    // Reactive pipeline combining MediaStore, Favorites, Sorting, Filtering, and Search
    val uiState: StateFlow<PhotosUiState> = combine(
        mediaWithFavoritesFlow,
        filterConfigFlow,
        _selectedIds
    ) { mediaList, config, selected ->
        var processed = mediaList

        // 1. Filter by media type
        processed = when (config.filter) {
            MediaTypeFilter.ALL -> processed
            MediaTypeFilter.PHOTOS_ONLY -> processed.filter { !it.isVideo }
            MediaTypeFilter.VIDEOS_ONLY -> processed.filter { it.isVideo }
            MediaTypeFilter.FAVORITES_ONLY -> processed.filter { it.isFavorite }
        }

        // 2. Search query filter
        if (config.query.isNotBlank()) {
            val q = config.query.trim().lowercase()
            processed = processed.filter {
                it.displayName.lowercase().contains(q) ||
                it.bucketDisplayName.lowercase().contains(q)
            }
        }

        // 3. Sort
        processed = when (config.sortBy) {
            SortBy.DATE_DESC -> processed.sortedByDescending { it.bestTimestamp }
            SortBy.DATE_ASC -> processed.sortedBy { it.bestTimestamp }
            SortBy.NAME_ASC -> processed.sortedBy { it.displayName.lowercase() }
            SortBy.NAME_DESC -> processed.sortedByDescending { it.displayName.lowercase() }
            SortBy.SIZE_DESC -> processed.sortedByDescending { it.size }
            SortBy.SIZE_ASC -> processed.sortedBy { it.size }
        }

        PhotosUiState(
            mediaItems = processed,
            isLoading = false,
            selectedIds = selected,
            gridColumns = config.columns,
            sortBy = config.sortBy,
            mediaTypeFilter = config.filter,
            searchQuery = config.query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PhotosUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sortBy: SortBy) {
        preferences.setSortBy(sortBy)
    }

    fun setMediaTypeFilter(filter: MediaTypeFilter) {
        preferences.setMediaTypeFilter(filter)
    }

    fun cycleGridColumns() {
        val current = preferences.gridColumns.value
        val next = when (current) {
            2 -> 3
            3 -> 4
            4 -> 5
            else -> 2
        }
        preferences.setGridColumns(next)
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (id in current) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun selectAll() {
        val allIds = uiState.value.mediaItems.map { it.id }.toSet()
        _selectedIds.value = allIds
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            if (item.isFavorite) {
                favoriteDao.deleteFavorite(item.id)
            } else {
                favoriteDao.insertFavorite(
                    FavoriteEntity(mediaId = item.id, uriString = item.uri.toString())
                )
            }
        }
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
                                successfulOriginalPaths.forEach { java.io.File(it).delete() }
                                viewModelScope.launch {
                                    favoriteDao.deleteFavorites(items.map { it.id })
                                }
                            }
                        }
                    }
                    is MediaOperationResult.Success -> {
                        successfulOriginalPaths.forEach { java.io.File(it).delete() }
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

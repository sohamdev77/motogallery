package com.motogallery.app.ui.screens.viewer

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.local.entity.FavoriteEntity
import com.motogallery.app.data.mediastore.ExifParser
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaViewerUiState(
    val mediaList: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val currentMetadata: MediaMetadata? = null,
    val isLoading: Boolean = true,
    val isBarsVisible: Boolean = true
) {
    val currentItem: MediaItem? get() = mediaList.getOrNull(currentIndex)
}

class MediaViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val repository = MediaStoreRepository(app)
    private val favoriteDao = app.database.favoriteDao()
    private val operationsHelper = MediaOperationsHelper(app)

    private val _uiState = MutableStateFlow(MediaViewerUiState())
    val uiState: StateFlow<MediaViewerUiState> = _uiState.asStateFlow()

    fun loadMedia(initialMediaId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val allMedia = withContext(Dispatchers.IO) { repository.queryAllMedia() }
            val favIds = favoriteDao.getAllFavoriteIds().toSet()
            val tagged = allMedia.map { it.copy(isFavorite = it.id in favIds) }

            val index = tagged.indexOfFirst { it.id == initialMediaId }.coerceAtLeast(0)

            _uiState.value = _uiState.value.copy(
                mediaList = tagged,
                currentIndex = index,
                isLoading = false
            )

            loadMetadataForCurrent()
        }
    }

    fun onPageChanged(index: Int) {
        if (index != _uiState.value.currentIndex) {
            _uiState.value = _uiState.value.copy(
                currentIndex = index,
                currentMetadata = null
            )
            loadMetadataForCurrent()
        }
    }

    fun toggleBarsVisibility() {
        _uiState.value = _uiState.value.copy(
            isBarsVisible = !_uiState.value.isBarsVisible
        )
    }

    fun loadMetadataForCurrent() {
        val item = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            val meta = ExifParser.parseMetadata(app, item)
            _uiState.value = _uiState.value.copy(currentMetadata = meta)
        }
    }

    fun toggleFavoriteCurrent() {
        val item = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            val isFav = !item.isFavorite
            if (isFav) {
                favoriteDao.insertFavorite(
                    FavoriteEntity(mediaId = item.id, uriString = item.uri.toString())
                )
            } else {
                favoriteDao.deleteFavorite(item.id)
            }

            // Update item in list
            val updatedList = _uiState.value.mediaList.toMutableList()
            updatedList[_uiState.value.currentIndex] = item.copy(isFavorite = isFav)
            _uiState.value = _uiState.value.copy(mediaList = updatedList)
        }
    }

    fun requestDeleteCurrent(
        onConsentRequired: (IntentSender) -> Unit,
        onDeleted: () -> Unit
    ) {
        val item = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            when (val res = operationsHelper.requestTrashMedia(listOf(item.uri), trash = true)) {
                is MediaOperationResult.RequiresConsent -> {
                    onConsentRequired(res.intentSender)
                }
                is MediaOperationResult.Success -> {
                    onDeleted()
                }
                else -> {}
            }
        }
    }

    fun renameCurrent(
        newName: String,
        onConsentRequired: (IntentSender) -> Unit,
        onSuccess: () -> Unit
    ) {
        val item = _uiState.value.currentItem ?: return
        viewModelScope.launch {
            when (val res = operationsHelper.renameMedia(item, newName)) {
                is MediaOperationResult.RequiresConsent -> {
                    onConsentRequired(res.intentSender)
                }
                is MediaOperationResult.Success -> {
                    onSuccess()
                }
                else -> {}
            }
        }
    }
}

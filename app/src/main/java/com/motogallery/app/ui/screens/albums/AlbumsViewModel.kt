package com.motogallery.app.ui.screens.albums

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.local.entity.CustomAlbumEntity
import com.motogallery.app.data.local.entity.CustomAlbumItemEntity
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.Album
import com.motogallery.app.data.model.AlbumType
import com.motogallery.app.data.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlbumsUiState(
    val deviceAlbums: List<Album> = emptyList(),
    val customAlbums: List<Album> = emptyList(),
    val favoriteCount: Int = 0,
    val favoriteCoverUri: Uri? = null,
    val videoCount: Int = 0,
    val videoCoverUri: Uri? = null,
    val isLoading: Boolean = true
)

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val repository = MediaStoreRepository(app)
    private val customAlbumDao = app.database.customAlbumDao()
    private val favoriteDao = app.database.favoriteDao()

    val uiState: StateFlow<AlbumsUiState> = combine(
        repository.observeMedia(),
        favoriteDao.getAllFavoriteIdsFlow(),
        customAlbumDao.getAllAlbumsFlow()
    ) { mediaList, favoriteIds, customAlbumsList ->
        val favoriteIdSet = favoriteIds.toSet()
        val favorites = mediaList.filter { it.id in favoriteIdSet }
        val videos = mediaList.filter { it.isVideo }

        val deviceFolders = repository.extractDeviceAlbums(mediaList)

        val customAlbumModels = customAlbumsList.map { entity ->
            val count = customAlbumDao.getItemCountForAlbum(entity.id)
            val coverStr = customAlbumDao.getCoverUriForAlbum(entity.id)
            Album(
                id = "custom_${entity.id}",
                name = entity.name,
                coverUri = coverStr?.let { Uri.parse(it) },
                itemCount = count,
                type = AlbumType.CUSTOM
            )
        }

        AlbumsUiState(
            deviceAlbums = deviceFolders,
            customAlbums = customAlbumModels,
            favoriteCount = favorites.size,
            favoriteCoverUri = favorites.firstOrNull()?.uri,
            videoCount = videos.size,
            videoCoverUri = videos.firstOrNull()?.uri,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState()
    )

    fun createCustomAlbum(name: String) {
        viewModelScope.launch {
            customAlbumDao.insertAlbum(CustomAlbumEntity(name = name))
        }
    }

    fun deleteCustomAlbum(albumId: Long) {
        viewModelScope.launch {
            customAlbumDao.deleteAlbum(albumId)
            customAlbumDao.deleteAlbumItems(albumId)
        }
    }

    fun addItemsToCustomAlbum(albumId: Long, items: List<MediaItem>) {
        viewModelScope.launch {
            val entities = items.map {
                CustomAlbumItemEntity(
                    albumId = albumId,
                    mediaId = it.id,
                    uriString = it.uri.toString()
                )
            }
            customAlbumDao.insertItems(entities)
        }
    }

    suspend fun getMediaForAlbum(albumId: String): List<MediaItem> {
        val allMedia = repository.queryAllMedia()
        val favIds = favoriteDao.getAllFavoriteIds().toSet()
        val tagged = allMedia.map { it.copy(isFavorite = it.id in favIds) }

        return when {
            albumId == "favorites" -> tagged.filter { it.isFavorite }
            albumId == "videos" -> tagged.filter { it.isVideo }
            albumId.startsWith("custom_") -> {
                val cId = albumId.removePrefix("custom_").toLongOrNull() ?: return emptyList()
                val customMediaIds = customAlbumDao.getCoverUriForAlbum(cId) // or query ids
                tagged.filter { it.id == cId }
            }
            else -> tagged.filter { it.bucketId == albumId }
        }
    }
}

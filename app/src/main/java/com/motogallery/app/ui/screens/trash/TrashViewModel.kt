package com.motogallery.app.ui.screens.trash

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.TrashedMediaItem
import com.motogallery.app.data.vault.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrashUiState(
    val publicTrashItems: List<TrashedMediaItem> = emptyList(),
    val vaultTrashItems: List<TrashedMediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val allTrashItems: List<TrashedMediaItem> get() = publicTrashItems + vaultTrashItems
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectedItems: List<TrashedMediaItem> get() = allTrashItems.filter { it.id in selectedIds }
}

class TrashViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val repository = MediaStoreRepository(app)
    private val operationsHelper = MediaOperationsHelper(app)
    private val vaultManager = app.vaultManager

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _publicTrash = MutableStateFlow<List<TrashedMediaItem>>(emptyList())
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<TrashUiState> = combine(
        _publicTrash,
        vaultManager.trashedItemsFlow,
        _selectedIds,
        _statusMessage,
        _errorMessage
    ) { pubTrash, vaultTrashed, selected, status, err ->
        val vaultConverted = vaultTrashed.map { vItem ->
            TrashedMediaItem(
                id = -vItem.id, // Negative ID to distinguish from MediaStore IDs
                uri = Uri.EMPTY,
                displayName = vItem.originalName,
                mimeType = vItem.mimeType,
                size = vItem.size,
                isVideo = vItem.isVideo,
                trashedTimeMs = vItem.trashedTimestamp,
                isVaultItem = true,
                vaultItem = vItem
            )
        }

        TrashUiState(
            publicTrashItems = pubTrash,
            vaultTrashItems = vaultConverted,
            selectedIds = selected,
            isLoading = false,
            statusMessage = status,
            errorMessage = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashUiState()
    )

    init {
        refreshPublicTrash()
    }

    fun refreshPublicTrash() {
        viewModelScope.launch {
            val pub = repository.queryPublicTrashedMedia()
            _publicTrash.value = pub
        }
    }

    fun toggleSelection(id: Long) {
        val cur = _selectedIds.value.toMutableSet()
        if (id in cur) cur.remove(id) else cur.add(id)
        _selectedIds.value = cur
    }

    fun selectAll() {
        val allIds = uiState.value.allTrashItems.map { it.id }.toSet()
        _selectedIds.value = allIds
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun restoreSelected(
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val selected = uiState.value.selectedItems
            val vaultItems = selected.filter { it.isVaultItem && it.vaultItem != null }
            val publicItems = selected.filter { !it.isVaultItem }

            // Restore vault items
            for (v in vaultItems) {
                vaultManager.restoreFromTrash(v.vaultItem!!.id)
            }

            // Restore public items via MediaStore untrash request
            if (publicItems.isNotEmpty()) {
                val uris = publicItems.map { it.uri }
                when (val res = operationsHelper.requestTrashMedia(uris, trash = false)) {
                    is MediaOperationResult.RequiresConsent -> {
                        onRequestConsent(res.intentSender) { granted ->
                            if (granted) refreshPublicTrash()
                        }
                    }
                    is MediaOperationResult.Success -> {
                        refreshPublicTrash()
                    }
                    is MediaOperationResult.Error -> {
                        _errorMessage.value = res.message
                    }
                }
            }

            clearSelection()
            onSuccess()
        }
    }

    fun permanentlyDeleteSelected(
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val selected = uiState.value.selectedItems
            val vaultItems = selected.filter { it.isVaultItem && it.vaultItem != null }
            val publicItems = selected.filter { !it.isVaultItem }

            // Permanently shred and delete vault items
            for (v in vaultItems) {
                vaultManager.deletePermanently(v.vaultItem!!.id)
            }

            // Permanently delete public items via system consent
            if (publicItems.isNotEmpty()) {
                val uris = publicItems.map { it.uri }
                when (val res = operationsHelper.requestDeleteMedia(uris)) {
                    is MediaOperationResult.RequiresConsent -> {
                        onRequestConsent(res.intentSender) { granted ->
                            if (granted) {
                                refreshPublicTrash()
                            }
                        }
                    }
                    is MediaOperationResult.Success -> {
                        refreshPublicTrash()
                    }
                    is MediaOperationResult.Error -> {
                        _errorMessage.value = res.message
                    }
                }
            }

            clearSelection()
            onSuccess()
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}

package com.motogallery.app.ui.screens.vault

import android.app.Application
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.VaultItem
import com.motogallery.app.data.vault.BiometricAuthManager
import com.motogallery.app.data.vault.VaultCryptoManager
import com.motogallery.app.data.vault.VaultLockManager
import com.motogallery.app.data.vault.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val isVaultConfigured: Boolean = false,
    val isUnlocked: Boolean = false,
    val items: List<VaultItem> = emptyList(),
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectedItems: List<VaultItem> get() = items.filter { it.id in selectedIds }
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val preferences = app.preferences
    private val cryptoManager = app.vaultCryptoManager
    private val vaultLockManager = app.vaultLockManager
    private val vaultManager = app.vaultManager
    private val biometricManager = BiometricAuthManager(app)
    private val operationsHelper = MediaOperationsHelper(app)
    private val mediaStoreRepository = MediaStoreRepository(app)

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private data class VaultStatus(
        val selected: Set<Long>,
        val status: String?,
        val error: String?
    )

    private val statusFlow = combine(_selectedIds, _statusMessage, _errorMessage) { selected, status, err ->
        VaultStatus(selected, status, err)
    }

    val uiState: StateFlow<VaultUiState> = combine(
        vaultManager.activeItemsFlow,
        vaultLockManager.isUnlocked,
        preferences.vaultBiometricEnabled,
        statusFlow
    ) { items, isUnlocked, biometricPref, statusState ->
        VaultUiState(
            isVaultConfigured = preferences.isVaultSetup(),
            isUnlocked = isUnlocked,
            items = items,
            isBiometricEnabled = biometricPref,
            isBiometricAvailable = biometricManager.isBiometricAvailable(),
            selectedIds = statusState.selected,
            statusMessage = statusState.status,
            errorMessage = statusState.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState()
    )

    fun setupVault(pin: String): Boolean {
        if (pin.length < 4) {
            _errorMessage.value = "PIN must be at least 4 digits"
            return false
        }
        val salt = cryptoManager.generateSalt()
        val hash = cryptoManager.hashPassword(pin, salt)
        preferences.setVaultCredentials(hash, salt)
        vaultLockManager.unlock()
        _statusMessage.value = "Vault successfully initialized"
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val salt = preferences.getVaultPasswordSalt() ?: return false
        val expectedHash = preferences.getVaultPasswordHash() ?: return false

        val isValid = cryptoManager.verifyPassword(pin, salt, expectedHash)
        if (isValid) {
            vaultLockManager.unlock()
            _errorMessage.value = null
            return true
        } else {
            _errorMessage.value = "Incorrect PIN"
            return false
        }
    }

    fun unlockWithBiometrics() {
        vaultLockManager.unlock()
        _errorMessage.value = null
    }

    fun lockVault() {
        vaultLockManager.lock()
        _selectedIds.value = emptySet()
    }

    fun toggleSelection(id: Long) {
        val cur = _selectedIds.value.toMutableSet()
        if (id in cur) cur.remove(id) else cur.add(id)
        _selectedIds.value = cur
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun restoreSelected(onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            val selected = uiState.value.selectedItems
            var count = 0
            for (item in selected) {
                val res = vaultManager.restoreToPublicGallery(item)
                if (res.isSuccess) count++
            }
            clearSelection()
            onSuccess(count)
        }
    }

    fun moveSelectedToTrash() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            vaultManager.moveMultipleToTrash(ids)
            clearSelection()
            _statusMessage.value = "${ids.size} item(s) moved to Vault Trash (30 days retention)"
        }
    }

    fun queryAvailablePublicMedia(): List<MediaItem> = mediaStoreRepository.queryAllMedia()

    fun importMediaItems(
        items: List<MediaItem>,
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
        onSuccess: (importedCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            var importedCount = 0
            val urisToDelete = mutableListOf<Uri>()
            val pathsToDelete = mutableListOf<String>()

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
                    urisToDelete.add(item.uri)
                    if (item.path.isNotBlank()) pathsToDelete.add(item.path)
                }
            }

            if (importedCount > 0) {
                onSuccess(importedCount)
                // Delete public originals so they are ONLY visible in the vault
                when (val delRes = operationsHelper.requestDeleteMedia(urisToDelete, pathsToDelete)) {
                    is MediaOperationResult.RequiresConsent -> {
                        onRequestConsent(delRes.intentSender) { granted ->
                            if (granted) {
                                pathsToDelete.forEach { java.io.File(it).delete() }
                                _statusMessage.value = "Imported $importedCount item(s). Public original(s) deleted."
                            } else {
                                _errorMessage.value = "Imported into vault, but permission to delete public files was declined."
                            }
                        }
                    }
                    is MediaOperationResult.Success -> {
                        pathsToDelete.forEach { java.io.File(it).delete() }
                        _statusMessage.value = "Imported $importedCount item(s). Public original(s) deleted."
                    }
                    is MediaOperationResult.Error -> {
                        pathsToDelete.forEach { java.io.File(it).delete() }
                        _statusMessage.value = "Imported $importedCount item(s) into vault."
                    }
                }
            }
        }
    }

    fun importMedia(
        uri: Uri,
        displayName: String,
        mimeType: String,
        isVideo: Boolean,
        size: Long,
        onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit
    ) {
        viewModelScope.launch {
            val res = vaultManager.importMedia(
                sourceUri = uri,
                originalName = displayName,
                mimeType = mimeType,
                isVideo = isVideo,
                size = size
            )
            if (res.isSuccess) {
                val allPublic = mediaStoreRepository.queryAllMedia()
                val matchingItem = allPublic.find {
                    it.displayName == displayName || (it.size > 0 && it.size == size)
                }
                val resolvedUri = matchingItem?.uri ?: operationsHelper.resolveToMediaStoreUri(uri)
                val paths = if (!matchingItem?.path.isNullOrBlank()) listOf(matchingItem!!.path) else emptyList()

                when (val delRes = operationsHelper.requestDeleteMedia(listOf(resolvedUri), paths)) {
                    is MediaOperationResult.RequiresConsent -> {
                        onRequestConsent(delRes.intentSender) { granted ->
                            if (granted) {
                                paths.forEach { java.io.File(it).delete() }
                                _statusMessage.value = "Secured into vault and deleted public copy."
                            }
                        }
                    }
                    is MediaOperationResult.Success -> {
                        paths.forEach { java.io.File(it).delete() }
                        _statusMessage.value = "Secured into vault and deleted public copy."
                    }
                    else -> {
                        _statusMessage.value = "Encrypted and secured into vault."
                    }
                }
            } else {
                _errorMessage.value = "Failed to encrypt media: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    suspend fun getDecryptedBytes(item: VaultItem): ByteArray? {
        return try {
            vaultManager.decryptToMemory(item)
        } catch (e: Exception) {
            _errorMessage.value = "Decryption error: ${e.message}"
            null
        }
    }

    suspend fun getDecryptedThumbnail(item: VaultItem): ByteArray? {
        return vaultManager.decryptThumbnailToMemory(item)
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}

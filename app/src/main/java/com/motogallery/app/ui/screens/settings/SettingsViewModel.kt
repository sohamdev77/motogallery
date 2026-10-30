package com.motogallery.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.mediastore.MediaStoreRepository
import com.motogallery.app.data.vault.BiometricAuthManager
import com.motogallery.app.data.vault.VaultCryptoManager
import com.motogallery.app.data.vault.VaultManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val useDynamicColors: Boolean = false,
    val gridColumns: Int = 3,
    val isVaultConfigured: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isBiometricSupported: Boolean = false,
    val autoLockDelaySeconds: Int = 0,
    val isHalloweenTheme: Boolean = false,
    val isSpecialAnimations: Boolean = true,
    val isFlyingBats: Boolean = true,
    val isPumpkinEasterEgg: Boolean = true,
    val photoCount: Int = 0,
    val photoSizeBytes: Long = 0L,
    val videoCount: Int = 0,
    val videoSizeBytes: Long = 0L,
    val vaultSizeBytes: Long = 0L,
    val statusMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MotoGalleryApp
    private val preferences = app.preferences
    private val repository = MediaStoreRepository(app)
    private val vaultManager = app.vaultManager
    private val cryptoManager = app.vaultCryptoManager
    private val biometricManager = BiometricAuthManager(app)

    private val _photoStats = MutableStateFlow(Pair(0, 0L))
    private val _videoStats = MutableStateFlow(Pair(0, 0L))
    private val _vaultSize = MutableStateFlow(0L)
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private data class StorageStats(
        val photoStats: Pair<Int, Long>,
        val videoStats: Pair<Int, Long>,
        val vaultSize: Long
    )

    private val storageStatsFlow = combine(_photoStats, _videoStats, _vaultSize) { photoSt, videoSt, vSize ->
        StorageStats(photoSt, videoSt, vSize)
    }

    private data class PreferencesState(
        val isDarkTheme: Boolean,
        val useDynamicColors: Boolean,
        val gridColumns: Int,
        val isBiometricEnabled: Boolean,
        val autoLockDelaySeconds: Int,
        val isHalloweenTheme: Boolean,
        val isSpecialAnimations: Boolean,
        val isFlyingBats: Boolean,
        val isPumpkinEasterEgg: Boolean
    )

    private val preferencesFlow = combine(
        preferences.isDarkTheme,
        preferences.useDynamicColors,
        preferences.gridColumns,
        preferences.vaultBiometricEnabled,
        preferences.vaultAutoLockDelay,
        preferences.isHalloweenThemeEnabled,
        preferences.isSpecialAnimationsEnabled,
        preferences.isFlyingBatsEnabled,
        preferences.isPumpkinEasterEggEnabled
    ) { args: Array<Any> ->
        PreferencesState(
            isDarkTheme = args[0] as Boolean,
            useDynamicColors = args[1] as Boolean,
            gridColumns = args[2] as Int,
            isBiometricEnabled = args[3] as Boolean,
            autoLockDelaySeconds = args[4] as Int,
            isHalloweenTheme = args[5] as Boolean,
            isSpecialAnimations = args[6] as Boolean,
            isFlyingBats = args[7] as Boolean,
            isPumpkinEasterEgg = args[8] as Boolean
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesFlow,
        storageStatsFlow
    ) { prefs, stats ->
        SettingsUiState(
            isDarkTheme = prefs.isDarkTheme,
            useDynamicColors = prefs.useDynamicColors,
            gridColumns = prefs.gridColumns,
            isVaultConfigured = preferences.isVaultSetup(),
            isBiometricEnabled = prefs.isBiometricEnabled,
            isBiometricSupported = biometricManager.isBiometricAvailable(),
            autoLockDelaySeconds = prefs.autoLockDelaySeconds,
            isHalloweenTheme = prefs.isHalloweenTheme,
            isSpecialAnimations = prefs.isSpecialAnimations,
            isFlyingBats = prefs.isFlyingBats,
            isPumpkinEasterEgg = prefs.isPumpkinEasterEgg,
            photoCount = stats.photoStats.first,
            photoSizeBytes = stats.photoStats.second,
            videoCount = stats.videoStats.first,
            videoSizeBytes = stats.videoStats.second,
            vaultSizeBytes = stats.vaultSize
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        loadStorageBreakdown()
    }

    fun loadStorageBreakdown() {
        viewModelScope.launch {
            val allMedia = withContext(Dispatchers.IO) { repository.queryAllMedia() }
            val photos = allMedia.filter { !it.isVideo }
            val videos = allMedia.filter { it.isVideo }
            val vSize = vaultManager.getTotalVaultSizeBytes()

            _photoStats.value = Pair(photos.size, photos.sumOf { it.size })
            _videoStats.value = Pair(videos.size, videos.sumOf { it.size })
            _vaultSize.value = vSize
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        preferences.setDarkTheme(enabled)
    }

    fun setDynamicColors(enabled: Boolean) {
        preferences.setDynamicColors(enabled)
    }

    fun setGridColumns(columns: Int) {
        preferences.setGridColumns(columns)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.setVaultBiometricEnabled(enabled)
    }

    fun setAutoLockDelay(delaySeconds: Int) {
        preferences.setVaultAutoLockDelay(delaySeconds)
    }

    fun setHalloweenTheme(enabled: Boolean) {
        preferences.setHalloweenThemeEnabled(enabled)
    }

    fun setSpecialAnimations(enabled: Boolean) {
        preferences.setSpecialAnimationsEnabled(enabled)
    }

    fun setFlyingBats(enabled: Boolean) {
        preferences.setFlyingBatsEnabled(enabled)
    }

    fun setPumpkinEasterEgg(enabled: Boolean) {
        preferences.setPumpkinEasterEggEnabled(enabled)
    }

    fun changeVaultPin(oldPin: String, newPin: String): Boolean {
        val salt = preferences.getVaultPasswordSalt() ?: return false
        val expectedHash = preferences.getVaultPasswordHash() ?: return false

        if (!cryptoManager.verifyPassword(oldPin, salt, expectedHash)) {
            _statusMessage.value = "Incorrect existing PIN"
            return false
        }

        val newSalt = cryptoManager.generateSalt()
        val newHash = cryptoManager.hashPassword(newPin, newSalt)
        preferences.setVaultCredentials(newHash, newSalt)
        _statusMessage.value = "Vault PIN updated successfully"
        return true
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

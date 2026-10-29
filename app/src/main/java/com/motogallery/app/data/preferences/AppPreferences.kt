package com.motogallery.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.motogallery.app.data.model.MediaTypeFilter
import com.motogallery.app.data.model.SortBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "motogallery_prefs",
        Context.MODE_PRIVATE
    )

    private val _gridColumns = MutableStateFlow(prefs.getInt(KEY_GRID_COLUMNS, 3))
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK_THEME, true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLORS, false))
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private val _sortBy = MutableStateFlow(
        SortBy.valueOf(prefs.getString(KEY_SORT_BY, SortBy.DATE_DESC.name) ?: SortBy.DATE_DESC.name)
    )
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _mediaTypeFilter = MutableStateFlow(
        MediaTypeFilter.valueOf(prefs.getString(KEY_FILTER_TYPE, MediaTypeFilter.ALL.name) ?: MediaTypeFilter.ALL.name)
    )
    val mediaTypeFilter: StateFlow<MediaTypeFilter> = _mediaTypeFilter.asStateFlow()

    private val _vaultBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_VAULT_BIOMETRIC, false))
    val vaultBiometricEnabled: StateFlow<Boolean> = _vaultBiometricEnabled.asStateFlow()

    private val _vaultAutoLockDelay = MutableStateFlow(prefs.getInt(KEY_VAULT_AUTO_LOCK_DELAY, 0)) // 0 = Immediately
    val vaultAutoLockDelay: StateFlow<Int> = _vaultAutoLockDelay.asStateFlow()

    fun setGridColumns(columns: Int) {
        val clamped = columns.coerceIn(2, 5)
        prefs.edit().putInt(KEY_GRID_COLUMNS, clamped).apply()
        _gridColumns.value = clamped
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }

    fun setDynamicColors(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply()
        _useDynamicColors.value = enabled
    }

    fun setSortBy(sortBy: SortBy) {
        prefs.edit().putString(KEY_SORT_BY, sortBy.name).apply()
        _sortBy.value = sortBy
    }

    fun setMediaTypeFilter(filter: MediaTypeFilter) {
        prefs.edit().putString(KEY_FILTER_TYPE, filter.name).apply()
        _mediaTypeFilter.value = filter
    }

    // Vault Master Authentication Storage
    fun isVaultSetup(): Boolean {
        return prefs.contains(KEY_VAULT_PASSWORD_HASH) && prefs.contains(KEY_VAULT_PASSWORD_SALT)
    }

    fun setVaultCredentials(passwordHash: String, salt: String) {
        prefs.edit()
            .putString(KEY_VAULT_PASSWORD_HASH, passwordHash)
            .putString(KEY_VAULT_PASSWORD_SALT, salt)
            .apply()
    }

    fun getVaultPasswordHash(): String? = prefs.getString(KEY_VAULT_PASSWORD_HASH, null)
    fun getVaultPasswordSalt(): String? = prefs.getString(KEY_VAULT_PASSWORD_SALT, null)

    fun setVaultBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VAULT_BIOMETRIC, enabled).apply()
        _vaultBiometricEnabled.value = enabled
    }

    fun setVaultAutoLockDelay(delaySeconds: Int) {
        prefs.edit().putInt(KEY_VAULT_AUTO_LOCK_DELAY, delaySeconds).apply()
        _vaultAutoLockDelay.value = delaySeconds
    }

    // Video Playback Resume Points
    fun saveVideoPosition(videoUri: String, positionMs: Long) {
        prefs.edit().putLong("video_pos_$videoUri", positionMs).apply()
    }

    fun getVideoPosition(videoUri: String): Long {
        return prefs.getLong("video_pos_$videoUri", 0L)
    }

    companion object {
        private const val KEY_GRID_COLUMNS = "grid_columns"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
        private const val KEY_SORT_BY = "sort_by"
        private const val KEY_FILTER_TYPE = "filter_type"
        private const val KEY_VAULT_PASSWORD_HASH = "vault_pwd_hash"
        private const val KEY_VAULT_PASSWORD_SALT = "vault_pwd_salt"
        private const val KEY_VAULT_BIOMETRIC = "vault_biometric"
        private const val KEY_VAULT_AUTO_LOCK_DELAY = "vault_autolock_delay"
    }
}

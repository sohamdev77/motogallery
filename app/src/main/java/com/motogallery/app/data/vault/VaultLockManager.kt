package com.motogallery.app.data.vault

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VaultLockManager {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var lastBackgroundTimestamp = 0L

    fun unlock() {
        _isUnlocked.value = true
        lastBackgroundTimestamp = 0L
    }

    fun lock() {
        _isUnlocked.value = false
        lastBackgroundTimestamp = 0L
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppForegrounded(autoLockDelaySeconds: Int) {
        if (!_isUnlocked.value) return
        if (autoLockDelaySeconds <= 0) {
            // Lock immediately on background
            lock()
            return
        }
        val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000L
        if (elapsedSeconds >= autoLockDelaySeconds) {
            lock()
        }
    }
}

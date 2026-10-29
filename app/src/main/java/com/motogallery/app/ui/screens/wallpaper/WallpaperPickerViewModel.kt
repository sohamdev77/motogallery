package com.motogallery.app.ui.screens.wallpaper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.ui.theme.WallpaperTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WallpaperPickerUiState(
    val currentWallpaper: WallpaperTheme = WallpaperTheme.NONE,
    val dimming: Float = 0.35f,
    val themeMode: String = "dark",
    val accentColorHex: String = "",
    val previewingWallpaper: WallpaperTheme? = null,
    val isAppliedMessage: String? = null
)

class WallpaperPickerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = (application as MotoGalleryApp).preferences

    val wallpapers: List<WallpaperTheme> = WallpaperTheme.entries

    private val _previewWallpaper = MutableStateFlow<WallpaperTheme?>(null)
    val previewWallpaper: StateFlow<WallpaperTheme?> = _previewWallpaper.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val currentWallpaperId: StateFlow<String> = prefs.wallpaperId
    val wallpaperDimming: StateFlow<Float> = prefs.wallpaperDimming
    val themeMode: StateFlow<String> = prefs.themeMode
    val accentColor: StateFlow<String> = prefs.accentColor

    fun applyWallpaper(wallpaper: WallpaperTheme) {
        prefs.setWallpaperId(wallpaper.id)
        _statusMessage.value = "Applied ${wallpaper.displayName} theme"
    }

    fun setDimming(dimming: Float) {
        prefs.setWallpaperDimming(dimming)
    }

    fun setThemeMode(mode: String) {
        prefs.setThemeMode(mode)
    }

    fun setAccentColor(hex: String) {
        prefs.setAccentColor(hex)
    }

    fun setPreviewWallpaper(wallpaper: WallpaperTheme?) {
        _previewWallpaper.value = wallpaper
    }

    fun resetToDefault() {
        prefs.resetAppearanceDefaults()
        _statusMessage.value = "Reset appearance to default Obsidian Dark"
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

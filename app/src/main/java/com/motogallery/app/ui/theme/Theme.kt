package com.motogallery.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MotoViolet,
    onPrimary = MotoBackground,
    primaryContainer = MotoSurfaceVariant,
    onPrimaryContainer = MotoViolet,
    secondary = MotoPink,
    onSecondary = MotoBackground,
    secondaryContainer = MotoSurfaceHighlight,
    onSecondaryContainer = MotoPink,
    tertiary = MotoOrange,
    onTertiary = MotoBackground,
    background = MotoBackground,
    onBackground = MotoTextPrimary,
    surface = MotoSurface,
    onSurface = MotoTextPrimary,
    surfaceVariant = MotoSurfaceVariant,
    onSurfaceVariant = MotoTextSecondary,
    outline = MotoBorder,
    error = MotoError
)

private val LightColorScheme = lightColorScheme(
    primary = MotoViolet,
    onPrimary = MotoLightBackground,
    primaryContainer = MotoLightSurfaceVariant,
    onPrimaryContainer = MotoViolet,
    secondary = MotoPink,
    onSecondary = MotoLightBackground,
    tertiary = MotoOrange,
    background = MotoLightBackground,
    onBackground = MotoLightTextPrimary,
    surface = MotoLightSurface,
    onSurface = MotoLightTextPrimary,
    surfaceVariant = MotoLightSurfaceVariant,
    onSurfaceVariant = MotoLightTextSecondary
)

data class WallpaperConfig(
    val wallpaper: WallpaperTheme = WallpaperTheme.NONE,
    val dimming: Float = 0.35f,
    val blur: Float = 0.20f,
    val isWallpaperActive: Boolean = false,
    val backgroundColor: Color = MotoBackground,
    val surfaceColor: Color = MotoSurface,
    val topBarColor: Color = MotoBackground,
    val bottomBarColor: Color = MotoSurface
)

val LocalWallpaperConfig = staticCompositionLocalOf { WallpaperConfig() }

@Composable
fun MotoGalleryTheme(
    darkTheme: Boolean = true, // Dark-first by design
    dynamicColor: Boolean = false, // Curated palette by default
    wallpaperTheme: WallpaperTheme = WallpaperTheme.NONE,
    wallpaperDimming: Float = 0.35f,
    wallpaperBlur: Float = 0.20f,
    customAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isWallpaperActive = wallpaperTheme != WallpaperTheme.NONE

    val effectiveAccent = customAccentColor 
        ?: (if (isWallpaperActive) wallpaperTheme.primaryAccent else MotoViolet)

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.copy(
            primary = effectiveAccent,
            primaryContainer = effectiveAccent.copy(alpha = 0.2f),
            onPrimaryContainer = effectiveAccent
        )
        else -> LightColorScheme.copy(
            primary = effectiveAccent,
            primaryContainer = effectiveAccent.copy(alpha = 0.15f),
            onPrimaryContainer = effectiveAccent
        )
    }

    val wallpaperConfig = WallpaperConfig(
        wallpaper = wallpaperTheme,
        dimming = wallpaperDimming,
        blur = wallpaperBlur,
        isWallpaperActive = isWallpaperActive,
        backgroundColor = if (isWallpaperActive) Color.Transparent else (if (darkTheme) MotoBackground else MotoLightBackground),
        surfaceColor = if (isWallpaperActive) {
            if (darkTheme) MotoSurface.copy(alpha = 0.82f) else MotoLightSurface.copy(alpha = 0.85f)
        } else {
            if (darkTheme) MotoSurface else MotoLightSurface
        },
        topBarColor = if (isWallpaperActive) {
            if (darkTheme) MotoBackground.copy(alpha = 0.75f) else MotoLightBackground.copy(alpha = 0.80f)
        } else {
            if (darkTheme) MotoBackground else MotoLightBackground
        },
        bottomBarColor = if (isWallpaperActive) {
            if (darkTheme) MotoSurface.copy(alpha = 0.85f) else MotoLightSurface.copy(alpha = 0.90f)
        } else {
            if (darkTheme) MotoSurface else MotoLightSurface
        }
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val statusBarColor = if (isWallpaperActive) {
                    android.graphics.Color.TRANSPARENT
                } else {
                    baseColorScheme.background.toArgb()
                }
                val navBarColor = if (isWallpaperActive) {
                    android.graphics.Color.TRANSPARENT
                } else {
                    baseColorScheme.background.toArgb()
                }
                window.statusBarColor = statusBarColor
                window.navigationBarColor = navBarColor
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalWallpaperConfig provides wallpaperConfig) {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = Typography,
            content = content
        )
    }
}

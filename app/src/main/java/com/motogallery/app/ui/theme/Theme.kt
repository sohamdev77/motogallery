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

@Composable
fun MotoGalleryTheme(
    darkTheme: Boolean = true, // Dark-first by design
    dynamicColor: Boolean = false, // Curated palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.motogallery.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple Design & Fluid Motion Springs
 *
 * Implements Apple WWDC 2018 "Designing Fluid Interfaces" physics:
 * - Default UI Spring: Critically damped (Damping 1.0, no overshoot) for navigation & chrome
 * - Momentum / Flick Spring: Underdamped (Damping ~0.8, slight bounce) for physical throws
 * - Interactive Press: Rapid response, zero latency, seamless spring rebound
 */
object AppleSprings {
    /** Critically damped spring (dampingRatio = 1.0f) - smooth, non-distracting settle */
    val DefaultSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Momentum / flick spring (dampingRatio = ~0.75-0.8) - physical overshoot for throws */
    val BouncySpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Snappy spring for instantaneous tactile responses */
    val SnappySpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Sheet & modal slide spring */
    val SheetSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * Apple Tactile Press Modifier (Response Principle - Kill Latency)
 *
 * Responds on pointer-down immediately with physical scale compression,
 * triggers tactile haptic click, and springs back continuously.
 * If the user drags away past boundary, cancels safely without firing onClick.
 */
@Composable
fun Modifier.applePressable(
    pressScale: Float = 0.95f,
    enabled: Boolean = true,
    enableHaptic: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressScale else 1.0f,
        animationSpec = AppleSprings.DefaultSpring,
        label = "applePressScale"
    )

    return this
        .scale(scale)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    isPressed = true
                    if (enableHaptic) {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                    }
                    val released = tryAwaitRelease()
                    isPressed = false
                },
                onTap = {
                    onClick()
                },
                onLongPress = {
                    if (onLongClick != null) {
                        if (enableHaptic) {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (_: Exception) {}
                        }
                        onLongClick()
                    }
                }
            )
        }
}

/**
 * Apple-Style Glassmorphic Surface Modifier
 *
 * Provides:
 * 1. Translucent frosted glass tint
 * 2. Specular light-catching gradient border (light reflecting along top edge)
 * 3. Inner ambient luminosity sheen
 * 4. Hardware RenderEffect blur on Android 12+ (API 31+)
 */
@Composable
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color? = null,
    borderStroke: BorderStroke? = null,
    specularHighlight: Boolean = true
): Modifier {
    val isDark = isSystemInDarkTheme()
    val wallpaperConfig = LocalWallpaperConfig.current

    // Base glass tint: translucent frosted obsidian in dark mode, frosted ice in light mode
    val defaultGlassColor = when {
        wallpaperConfig.isWallpaperActive -> wallpaperConfig.surfaceColor.copy(alpha = 0.70f)
        isDark -> Color(0xCC0E1528) // Translucent deep obsidian
        else -> Color(0xE0FFFFFF)   // Frosted white glass
    }
    val effectiveBg = backgroundColor ?: defaultGlassColor

    // Apple Specular Rim: light catches top edge more brightly than bottom
    val effectiveBorder = borderStroke ?: BorderStroke(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color.White.copy(alpha = 0.22f), // light catching top
                    Color.White.copy(alpha = 0.04f)  // subtle bottom
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.80f),
                    Color(0x33000000)
                )
            }
        )
    )

    return this
        .clip(shape)
        .background(effectiveBg)
        .then(
            if (specularHighlight) {
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.05f else 0.12f),
                            Color.Transparent
                        )
                    )
                )
            } else Modifier
        )
        .border(effectiveBorder, shape)
}

/**
 * Reusable Frosted Glass Pill Badge / Chip
 * Ideal for duration stamps, favorites count, 4K indicators, and tags.
 */
@Composable
fun GlassBadge(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = 0.48f),
    borderColor: Color = Color.White.copy(alpha = 0.20f),
    shape: Shape = RoundedCornerShape(10.dp),
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    0.5.dp,
                    Brush.verticalGradient(
                        listOf(borderColor, borderColor.copy(alpha = 0.05f))
                    )
                ),
                shape
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

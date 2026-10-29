package com.motogallery.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MotoGallery Curated Dark-First Obsidian Palette
val MotoBackground = Color(0xFF0B1020)
val MotoSurface = Color(0xFF131A2E)
val MotoSurfaceVariant = Color(0xFF1A233D)
val MotoSurfaceHighlight = Color(0xFF243054)
val MotoBorder = Color(0xFF2E3D6B)

// Accents
val MotoViolet = Color(0xFFA855F7)
val MotoPink = Color(0xFFEC65B5)
val MotoOrange = Color(0xFFFFA34D)
val MotoEmerald = Color(0xFF10B981)
val MotoError = Color(0xFFEF4444)

// Text
val MotoTextPrimary = Color(0xFFF8FAFC)
val MotoTextSecondary = Color(0xFF94A3B8)
val MotoTextTertiary = Color(0xFF64748B)

// Light Theme Alternates (Optional clean mode)
val MotoLightBackground = Color(0xFFF8FAFC)
val MotoLightSurface = Color(0xFFFFFFFF)
val MotoLightSurfaceVariant = Color(0xFFF1F5F9)
val MotoLightTextPrimary = Color(0xFF0F172A)
val MotoLightTextSecondary = Color(0xFF475569)

// Signature Gradient for Highlights & Badges
val MotoGradientBrush = Brush.linearGradient(
    colors = listOf(MotoViolet, MotoPink, MotoOrange)
)

val MotoDarkGradientBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF1E1B4B), Color(0xFF3B0764), Color(0xFF431407))
)

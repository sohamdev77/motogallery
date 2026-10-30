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

// Halloween Fun Seasonal Theme Palette
val MotoHalloweenOrange = Color(0xFFFF7518) // Signature Pumpkin Orange
val MotoHalloweenAmber = Color(0xFFFFA726)  // Warm Spooky Glow
val MotoHalloweenDarkBg = Color(0xFF090608) // Deep Eerie Obsidian Black
val MotoHalloweenSurface = Color(0xFF140D10) // Eerie Dark Surface
val MotoHalloweenSurfaceVariant = Color(0xFF22151B) // Spooky Card Surface
val MotoHalloweenSurfaceHighlight = Color(0xFF331C28)
val MotoHalloweenBorder = Color(0xFF4A253A)
val MotoHalloweenPurple = Color(0xFF2B0E38) // Soft Eerie Purple Shadow / Accent
val MotoHalloweenGlow = Color(0xFFFF9100)

val MotoHalloweenGradientBrush = Brush.linearGradient(
    colors = listOf(MotoHalloweenOrange, MotoHalloweenAmber, MotoHalloweenPurple)
)

// Apple-Style Glassmorphic Materials & Specular Sheens
val MotoGlassDark = Color(0xD90B1020)          // Translucent deep obsidian chrome
val MotoGlassSurface = Color(0xB8131A2E)       // Translucent elevated frosted surface
val MotoGlassBorder = Color(0x2EFFFFFF)        // 18% translucent specular edge
val MotoGlassHighlight = Color(0x1AFFFFFF)     // Subtle frosted glass highlight
val MotoGlassSpecularTop = Color(0x3BFFFFFF)   // ~23% white light-catching top edge
val MotoGlassSpecularBottom = Color(0x0AFFFFFF)// ~4% subtle bottom edge

val MotoGlassBorderBrush = Brush.verticalGradient(
    colors = listOf(MotoGlassSpecularTop, MotoGlassSpecularBottom)
)

val MotoGlassSheenBrush = Brush.verticalGradient(
    colors = listOf(Color.White.copy(alpha = 0.07f), Color.Transparent)
)



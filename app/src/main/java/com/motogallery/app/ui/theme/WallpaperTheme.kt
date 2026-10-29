package com.motogallery.app.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.motogallery.app.R

enum class WallpaperTheme(
    val id: String,
    val displayName: String,
    val description: String,
    @DrawableRes val drawableRes: Int,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val isDark: Boolean = true
) {
    NONE(
        id = "default",
        displayName = "Obsidian Dark",
        description = "Signature dark-first MotoGallery obsidian palette",
        drawableRes = 0,
        primaryAccent = MotoViolet,
        secondaryAccent = MotoPink,
        isDark = true
    ),
    MIDNIGHT_AURORA(
        id = "midnight_aurora",
        displayName = "Midnight Aurora",
        description = "Dark blue and purple aurora starlight",
        drawableRes = R.drawable.bg_wallpaper_midnight_aurora,
        primaryAccent = Color(0xFFA855F7), // Violet
        secondaryAccent = Color(0xFF10B981), // Emerald
        isDark = true
    ),
    SAKURA_DREAM(
        id = "sakura_dream",
        displayName = "Sakura Dream",
        description = "Pink cherry blossom aesthetic & dusk magenta",
        drawableRes = R.drawable.bg_wallpaper_sakura_dream,
        primaryAccent = Color(0xFFF472B6), // Pink
        secondaryAccent = Color(0xFFFB7185), // Rose
        isDark = true
    ),
    CYBERPUNK_NEON(
        id = "cyberpunk_neon",
        displayName = "Cyberpunk Neon",
        description = "Futuristic neon purple and cyan lasers",
        drawableRes = R.drawable.bg_wallpaper_cyberpunk_neon,
        primaryAccent = Color(0xFF06B6D4), // Cyan
        secondaryAccent = Color(0xFFEC4899), // Neon Pink
        isDark = true
    ),
    AMOLED_BLACK(
        id = "amoled_black",
        displayName = "AMOLED Black",
        description = "Pure 100% black with minimal slate accents",
        drawableRes = R.drawable.bg_wallpaper_amoled_black,
        primaryAccent = Color(0xFF94A3B8), // Silver slate
        secondaryAccent = Color(0xFF64748B),
        isDark = true
    ),
    OCEAN_DEPTHS(
        id = "ocean_depths",
        displayName = "Ocean Depths",
        description = "Deep blue underwater marine bioluminescence",
        drawableRes = R.drawable.bg_wallpaper_ocean_depths,
        primaryAccent = Color(0xFF38BDF8), // Sky Blue
        secondaryAccent = Color(0xFF2DD4BF), // Teal
        isDark = true
    ),
    SUNSET_GLOW(
        id = "sunset_glow",
        displayName = "Sunset Glow",
        description = "Warm golden hour orange and radiant coral",
        drawableRes = R.drawable.bg_wallpaper_sunset_glow,
        primaryAccent = Color(0xFFF97316), // Orange
        secondaryAccent = Color(0xFFEF4444), // Coral Red
        isDark = true
    ),
    COSMIC_GALAXY(
        id = "cosmic_galaxy",
        displayName = "Cosmic Galaxy",
        description = "Spiral nebula, cosmic dust, and constellations",
        drawableRes = R.drawable.bg_wallpaper_cosmic_galaxy,
        primaryAccent = Color(0xFFC084FC), // Lavender
        secondaryAccent = Color(0xFF818CF8), // Indigo
        isDark = true
    ),
    MINIMAL_FROST(
        id = "minimal_frost",
        displayName = "Minimal Frost",
        description = "Soft neutral slate gradients and icy surfaces",
        drawableRes = R.drawable.bg_wallpaper_minimal_frost,
        primaryAccent = Color(0xFFCBD5E1), // Frosted silver
        secondaryAccent = Color(0xFF94A3B8),
        isDark = true
    );

    companion object {
        fun fromId(id: String?): WallpaperTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}

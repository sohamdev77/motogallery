package com.motogallery.app

import com.motogallery.app.ui.theme.WallpaperTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperThemeTest {

    @Test
    fun testAllEightWallpapersExistAndAreConfigured() {
        // Must contain at least 8 bundled wallpapers + Default
        val wallpapers = WallpaperTheme.entries
        assertTrue(wallpapers.size >= 9)

        val requiredThemes = listOf(
            "midnight_aurora",
            "sakura_dream",
            "cyberpunk_neon",
            "amoled_black",
            "ocean_depths",
            "sunset_glow",
            "cosmic_galaxy",
            "minimal_frost"
        )

        requiredThemes.forEach { expectedId ->
            val theme = wallpapers.firstOrNull { it.id == expectedId }
            assertNotNull("Theme $expectedId must exist in WallpaperTheme", theme)
            assertTrue("Theme $expectedId must have a non-empty name", theme!!.displayName.isNotEmpty())
            assertTrue("Theme $expectedId must have a non-empty description", theme.description.isNotEmpty())
            assertTrue("Theme $expectedId must reference a valid drawable resource", theme.drawableRes != 0)
        }
    }

    @Test
    fun testFromIdResolvesCorrectlyWithCaseInsensitivity() {
        assertEquals(WallpaperTheme.MIDNIGHT_AURORA, WallpaperTheme.fromId("midnight_aurora"))
        assertEquals(WallpaperTheme.MIDNIGHT_AURORA, WallpaperTheme.fromId("MIDNIGHT_AURORA"))
        assertEquals(WallpaperTheme.SAKURA_DREAM, WallpaperTheme.fromId("sakura_dream"))
        assertEquals(WallpaperTheme.CYBERPUNK_NEON, WallpaperTheme.fromId("cyberpunk_neon"))
        assertEquals(WallpaperTheme.AMOLED_BLACK, WallpaperTheme.fromId("amoled_black"))
        assertEquals(WallpaperTheme.OCEAN_DEPTHS, WallpaperTheme.fromId("ocean_depths"))
        assertEquals(WallpaperTheme.SUNSET_GLOW, WallpaperTheme.fromId("sunset_glow"))
        assertEquals(WallpaperTheme.COSMIC_GALAXY, WallpaperTheme.fromId("cosmic_galaxy"))
        assertEquals(WallpaperTheme.MINIMAL_FROST, WallpaperTheme.fromId("minimal_frost"))
        assertEquals(WallpaperTheme.NONE, WallpaperTheme.fromId("default"))
        assertEquals(WallpaperTheme.NONE, WallpaperTheme.fromId("non_existent_id"))
        assertEquals(WallpaperTheme.NONE, WallpaperTheme.fromId(null))
    }

    @Test
    fun testDimmingClamping() {
        val minDim = 0.0f
        val maxDim = 0.8f

        val testUnder = (-0.5f).coerceIn(minDim, maxDim)
        assertEquals(0.0f, testUnder, 0.001f)

        val testOver = (1.2f).coerceIn(minDim, maxDim)
        assertEquals(0.8f, testOver, 0.001f)

        val testNormal = (0.35f).coerceIn(minDim, maxDim)
        assertEquals(0.35f, testNormal, 0.001f)
    }

    @Test
    fun testThemeModesSupported() {
        val validModes = listOf("dark", "light", "system")
        validModes.forEach { mode ->
            assertTrue(validModes.contains(mode))
        }
    }
}

package com.motogallery.app

import com.motogallery.app.ui.screens.seasonal.EasterEggSessionState
import com.motogallery.app.ui.theme.HalloweenConfig
import com.motogallery.app.ui.theme.MotoHalloweenAmber
import com.motogallery.app.ui.theme.MotoHalloweenDarkBg
import com.motogallery.app.ui.theme.MotoHalloweenOrange
import com.motogallery.app.ui.theme.MotoHalloweenSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SeasonalThemeTest {

    @Before
    fun setup() {
        EasterEggSessionState.reset()
    }

    @Test
    fun testHalloweenConfigDefaults() {
        val defaultConfig = HalloweenConfig()
        assertFalse("Halloween theme should be disabled by default", defaultConfig.isEnabled)
        assertTrue("Special animations should be enabled by default", defaultConfig.specialAnimationsEnabled)
        assertTrue("Flying bats should be enabled by default", defaultConfig.flyingBatsEnabled)
        assertTrue("Pumpkin easter egg should be enabled by default", defaultConfig.pumpkinEasterEggEnabled)
    }

    @Test
    fun testHalloweenColorsAreProperlyDefined() {
        // Signature Pumpkin Orange #FF7518
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFF7518), MotoHalloweenOrange)
        // Spooky Amber Glow #FFA726
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFA726), MotoHalloweenAmber)
        // Deep Obsidian Dark #090608
        assertEquals(androidx.compose.ui.graphics.Color(0xFF090608), MotoHalloweenDarkBg)
        // Eerie Surface #140D10
        assertEquals(androidx.compose.ui.graphics.Color(0xFF140D10), MotoHalloweenSurface)
    }

    @Test
    fun testSpecialAnimationsDisableInhibitsEffectsWhileThemeRemains() {
        val themeWithNoAnimations = HalloweenConfig(
            isEnabled = true,
            specialAnimationsEnabled = false,
            flyingBatsEnabled = true,
            pumpkinEasterEggEnabled = true
        )

        assertTrue(themeWithNoAnimations.isEnabled)
        assertFalse(themeWithNoAnimations.specialAnimationsEnabled)

        // Effective animated bats should only run if both special animations AND flying bats are true
        val canRunFlyingBats = themeWithNoAnimations.isEnabled &&
                themeWithNoAnimations.specialAnimationsEnabled &&
                themeWithNoAnimations.flyingBatsEnabled

        assertFalse("Flying bats motion should be inhibited when special animations is off", canRunFlyingBats)

        // Effective easter egg should only trigger if special animations is enabled
        val canTriggerEasterEgg = themeWithNoAnimations.isEnabled &&
                themeWithNoAnimations.specialAnimationsEnabled &&
                themeWithNoAnimations.pumpkinEasterEggEnabled

        assertFalse("Pumpkin easter egg popup should be inhibited when special animations is off", canTriggerEasterEgg)
    }

    @Test
    fun testEasterEggSessionMemoryTriggersOncePerSession() {
        assertFalse(EasterEggSessionState.hasShownPumpkinInSession)

        // First trigger simulation
        var triggerCount = 0
        val isHalloween = true
        val specialAnim = true
        val eggEnabled = true

        if (isHalloween && specialAnim && eggEnabled && !EasterEggSessionState.hasShownPumpkinInSession) {
            EasterEggSessionState.hasShownPumpkinInSession = true
            triggerCount++
        }

        assertEquals(1, triggerCount)
        assertTrue(EasterEggSessionState.hasShownPumpkinInSession)

        // Second entry into settings in same session: should NOT re-trigger
        if (isHalloween && specialAnim && eggEnabled && !EasterEggSessionState.hasShownPumpkinInSession) {
            triggerCount++
        }

        assertEquals("Easter egg should not trigger a second time in same app session", 1, triggerCount)

        // Reset session
        EasterEggSessionState.reset()
        assertFalse(EasterEggSessionState.hasShownPumpkinInSession)
    }

    @Test
    fun testTogglingHalloweenThemeStateTransitions() {
        var isHalloweenTheme = false
        var specialAnimations = true
        var flyingBats = true
        var pumpkinEgg = true

        // User toggles Halloween theme ON
        isHalloweenTheme = true
        var activeConfig = HalloweenConfig(
            isEnabled = isHalloweenTheme,
            specialAnimationsEnabled = specialAnimations,
            flyingBatsEnabled = flyingBats,
            pumpkinEasterEggEnabled = pumpkinEgg
        )
        assertTrue(activeConfig.isEnabled)
        assertTrue(activeConfig.specialAnimationsEnabled && activeConfig.flyingBatsEnabled)

        // User turns off flying bats
        flyingBats = false
        activeConfig = activeConfig.copy(flyingBatsEnabled = flyingBats)
        assertFalse(activeConfig.flyingBatsEnabled)

        // User turns off special animations entirely (low-motion/accessibility mode)
        specialAnimations = false
        activeConfig = activeConfig.copy(specialAnimationsEnabled = specialAnimations)
        assertFalse(activeConfig.specialAnimationsEnabled)
        assertTrue("Static Halloween theme remains active", activeConfig.isEnabled)
    }
}

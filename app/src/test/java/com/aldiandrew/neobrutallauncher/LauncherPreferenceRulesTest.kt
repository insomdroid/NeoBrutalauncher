package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherPreferenceRulesTest {
    @Test
    fun pinnedCountBelowMinimumUsesThree() {
        assertEquals(3, LauncherPreferenceRules.normalizePinnedCount(-1))
        assertEquals(3, LauncherPreferenceRules.normalizePinnedCount(2))
        assertEquals(3, LauncherPreferenceRules.normalizePinnedCount(3))
    }

    @Test
    fun pinnedCountAboveMinimumUsesFiveInFreeEdition() {
        assertEquals(5, LauncherPreferenceRules.normalizePinnedCount(4))
        assertEquals(5, LauncherPreferenceRules.normalizePinnedCount(5))
        assertEquals(5, LauncherPreferenceRules.normalizePinnedCount(8))
    }
}

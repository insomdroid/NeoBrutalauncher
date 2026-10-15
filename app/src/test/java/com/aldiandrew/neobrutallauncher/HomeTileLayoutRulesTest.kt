package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTileLayoutRulesTest {
    @Test
    fun layoutVariantIsStableForTheSameAppAndSize() {
        val first = HomeTileLayoutRules.variant("com.example.app", "MainActivity", "SMALL")
        val second = HomeTileLayoutRules.variant("com.example.app", "MainActivity", "SMALL")

        assertEquals(first, second)
        assertTrue(first in 0..4)
    }

    @Test
    fun layoutVariantRemainsWithinSupportedRangeForDifferentInputs() {
        val variants = listOf(
            HomeTileLayoutRules.variant("com.example.one", "MainActivity", "SMALL"),
            HomeTileLayoutRules.variant("com.example.one", "MainActivity", "HORIZONTAL"),
            HomeTileLayoutRules.variant("com.example.two", "MainActivity", "SMALL")
        )

        assertTrue(variants.all { it in 0..4 })
    }
}

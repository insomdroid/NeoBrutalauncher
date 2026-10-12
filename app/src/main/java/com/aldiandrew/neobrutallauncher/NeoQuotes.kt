package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.graphics.Color

data class NeoQuotePalette(
    val background: Color,
    val text: Color
)

object NeoQuotes {
    private val defaultQuotes = listOf(
        "Raw structure is not a flaw. It is the message.",
        "Make the grid visible. Make the interface honest.",
        "Strong borders turn space into architecture.",
        "Function first, decoration second, apology never.",
        "Break the polish. Keep the hierarchy."
    )

    fun builtInQuotes(): List<String> = defaultQuotes

    fun quoteForRotation(rotation: Int, slot: Int): String {
        val index = Math.floorMod(rotation + slot, defaultQuotes.size)
        return defaultQuotes[index]
    }

    fun pairForRotation(rotation: Int): Pair<String, String> =
        quoteForRotation(rotation, 0) to quoteForRotation(rotation, 1)

    fun paletteForRotation(rotation: Int): NeoQuotePalette {
        val backgrounds = BrutalColors.appPalette(0)
        val background = backgrounds[Math.floorMod(rotation, backgrounds.size)]
        return NeoQuotePalette(background, BrutalColors.Ink)
    }

}

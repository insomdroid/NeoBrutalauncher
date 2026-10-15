package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class NeoQuotesTest {
    @Test
    fun quoteRotationWrapsAndKeepsStableOrder() {
        val quotes = NeoQuotes.builtInQuotes()

        assertEquals(quotes[0], NeoQuotes.quoteForRotation(0, 0))
        assertEquals(quotes[0], NeoQuotes.quoteForRotation(quotes.size, 0))
        assertEquals(quotes[1], NeoQuotes.quoteForRotation(0, 1))
    }
}

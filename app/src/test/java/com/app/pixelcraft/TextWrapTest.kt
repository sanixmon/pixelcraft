package com.app.pixelcraft

import com.app.pixelcraft.domain.TextSizing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextWrapTest {
    // One unit per character keeps the expectations readable.
    private val measure: (String) -> Float = { it.length.toFloat() }

    @Test
    fun noBoxKeepsExplicitLines() {
        assertEquals(listOf("hello world", "again"), TextSizing.wrapLines("hello world\nagain", 0f, measure))
    }

    @Test
    fun wordsWrapToBoxWidth() {
        assertEquals(listOf("the quick", "brown fox"), TextSizing.wrapLines("the quick brown fox", 10f, measure))
    }

    @Test
    fun overlongWordIsSplit() {
        val lines = TextSizing.wrapLines("abcdefghij", 4f, measure)
        assertEquals("abcdefghij", lines.joinToString(""))
        assertTrue(lines.all { it.length <= 4 })
    }
}

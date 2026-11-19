package com.sudutcorp.sudutbulat.core.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputFormattersTest {

    @Test
    fun testThousandSeparatorFormattingAndOffsets() {
        val transformation = ThousandSeparatorVisualTransformation()

        val empty = transformation.filter(AnnotatedString(""))
        assertEquals("", empty.text.text)

        val small = transformation.filter(AnnotatedString("500"))
        assertEquals("500", small.text.text)

        val million = transformation.filter(AnnotatedString("2000000"))
        assertEquals("2.000.000", million.text.text)
        assertEquals(0, million.offsetMapping.originalToTransformed(0))
        assertEquals(1, million.offsetMapping.originalToTransformed(1))
        assertEquals(3, million.offsetMapping.originalToTransformed(2))
        assertEquals(9, million.offsetMapping.originalToTransformed(7))
        assertEquals(7, million.offsetMapping.transformedToOriginal(9))
        assertEquals(1, million.offsetMapping.transformedToOriginal(2))
    }

    @Test
    fun testTimeFormattingAndValidation() {
        val transformation = TimeVisualTransformation()

        val partial = transformation.filter(AnnotatedString("08"))
        assertEquals("08", partial.text.text)

        val full = transformation.filter(AnnotatedString("0830"))
        assertEquals("08.30", full.text.text)
        assertEquals(5, full.offsetMapping.originalToTransformed(4))
        assertEquals(4, full.offsetMapping.transformedToOriginal(5))

        assertTrue(isValidRawTime("0800"))
        assertTrue(isValidRawTime("2359"))
        assertFalse(isValidRawTime("2400"))
        assertFalse(isValidRawTime("0860"))
        assertFalse(isValidRawTime("08"))
        assertEquals("08.00", formatRawTimeToDisplay("0800", "09.00"))
        assertEquals("09.00", formatRawTimeToDisplay("08", "09.00"))
    }
}

package dev.hsichen.colorinvo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Code39Test {
    @Test fun includesStartAndStopSymbols() {
        val (bars, width) = Code39.bars("/ABC1234")
        assertTrue(bars.isNotEmpty())
        assertTrue(width > 0)
        assertEquals(10, bars.first().startsAt)
        assertEquals(10, width - bars.last().startsAt - bars.last().width)
    }

    @Test fun invalidInputNeverProducesAPartialBarcodeOrCrashes() {
        listOf("_", "ABC💛", "A*B", "").forEach {
            assertTrue(Code39.bars(it).first.isEmpty())
            assertEquals(0, Code39.bars(it).second)
        }
    }
}

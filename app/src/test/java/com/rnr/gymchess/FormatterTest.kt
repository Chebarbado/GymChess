package com.rnr.gymchess

import com.rnr.gymchess.util.formatHistoryDate
import com.rnr.gymchess.util.formatTimeMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatterTest {

    @Test
    fun formatTimeMs_formatsMinutesAndSeconds() {
        assertEquals("00:00", formatTimeMs(0))
        assertEquals("01:05", formatTimeMs(65_000))
        assertEquals("10:00", formatTimeMs(600_000))
    }

    @Test
    fun formatTimeMs_neverShowsNegativeValues() {
        assertEquals("00:00", formatTimeMs(-5_000))
    }

    @Test
    fun formatHistoryDate_usesDayMonthYearPattern() {
        val formatted = formatHistoryDate(1_704_067_200_000L)

        assertTrue(formatted.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}")))
    }
}

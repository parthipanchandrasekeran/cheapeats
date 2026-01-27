package com.parthipan.cheapeats.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PriceSource enum and related functionality.
 */
class PriceSourceTest {

    // ==================== Enum Values Tests ====================

    @Test
    fun `PriceSource has all expected values`() {
        val values = PriceSource.values()

        assertEquals(4, values.size)
        assertTrue(values.contains(PriceSource.API_VERIFIED))
        assertTrue(values.contains(PriceSource.USER_REPORTED))
        assertTrue(values.contains(PriceSource.ESTIMATED))
        assertTrue(values.contains(PriceSource.UNKNOWN))
    }

    @Test
    fun `PriceSource valueOf works correctly`() {
        assertEquals(PriceSource.API_VERIFIED, PriceSource.valueOf("API_VERIFIED"))
        assertEquals(PriceSource.USER_REPORTED, PriceSource.valueOf("USER_REPORTED"))
        assertEquals(PriceSource.ESTIMATED, PriceSource.valueOf("ESTIMATED"))
        assertEquals(PriceSource.UNKNOWN, PriceSource.valueOf("UNKNOWN"))
    }

    // ==================== Display Label Tests ====================

    @Test
    fun `API_VERIFIED displays as Verified`() {
        assertEquals("Verified", PriceSource.API_VERIFIED.toDisplayLabel())
    }

    @Test
    fun `USER_REPORTED displays as Reported`() {
        assertEquals("Reported", PriceSource.USER_REPORTED.toDisplayLabel())
    }

    @Test
    fun `ESTIMATED displays as Estimated`() {
        assertEquals("Estimated", PriceSource.ESTIMATED.toDisplayLabel())
    }

    @Test
    fun `UNKNOWN displays as empty string`() {
        assertEquals("", PriceSource.UNKNOWN.toDisplayLabel())
    }

    // ==================== Trust Ordering Tests ====================

    @Test
    fun `API_VERIFIED is most trusted source`() {
        val sources = PriceSource.values()
        assertEquals(PriceSource.API_VERIFIED, sources[0])
    }

    @Test
    fun `UNKNOWN is least trusted source`() {
        val sources = PriceSource.values()
        assertEquals(PriceSource.UNKNOWN, sources[sources.size - 1])
    }
}

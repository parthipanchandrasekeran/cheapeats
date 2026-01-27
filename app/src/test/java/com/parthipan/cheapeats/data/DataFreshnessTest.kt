package com.parthipan.cheapeats.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DataFreshness enum and price confidence labels.
 */
class DataFreshnessTest {

    // ==================== Enum Values Tests ====================

    @Test
    fun `DataFreshness has all expected values`() {
        val values = DataFreshness.values()

        assertEquals(4, values.size)
        assertTrue(values.contains(DataFreshness.LIVE))
        assertTrue(values.contains(DataFreshness.RECENT))
        assertTrue(values.contains(DataFreshness.CACHED))
        assertTrue(values.contains(DataFreshness.UNKNOWN))
    }

    @Test
    fun `DataFreshness valueOf works correctly`() {
        assertEquals(DataFreshness.LIVE, DataFreshness.valueOf("LIVE"))
        assertEquals(DataFreshness.RECENT, DataFreshness.valueOf("RECENT"))
        assertEquals(DataFreshness.CACHED, DataFreshness.valueOf("CACHED"))
        assertEquals(DataFreshness.UNKNOWN, DataFreshness.valueOf("UNKNOWN"))
    }

    // ==================== Price Confidence Label Tests ====================

    /**
     * Helper to get price confidence text based on DataFreshness.
     * Mirrors the logic in RestaurantDetailScreen.
     */
    private fun getPriceConfidence(freshness: DataFreshness): Pair<String, String> {
        return when (freshness) {
            DataFreshness.LIVE -> "Price verified" to "Checked just now"
            DataFreshness.RECENT -> "Price verified" to "Updated within the hour"
            DataFreshness.CACHED -> "Price may vary" to "Last checked a while ago"
            DataFreshness.UNKNOWN -> "Price unverified" to "Confirm before ordering"
        }
    }

    @Test
    fun `LIVE freshness shows high confidence label`() {
        val (label, supporting) = getPriceConfidence(DataFreshness.LIVE)

        assertEquals("Price verified", label)
        assertEquals("Checked just now", supporting)
    }

    @Test
    fun `RECENT freshness shows high confidence label`() {
        val (label, supporting) = getPriceConfidence(DataFreshness.RECENT)

        assertEquals("Price verified", label)
        assertEquals("Updated within the hour", supporting)
    }

    @Test
    fun `CACHED freshness shows medium confidence label`() {
        val (label, supporting) = getPriceConfidence(DataFreshness.CACHED)

        assertEquals("Price may vary", label)
        assertEquals("Last checked a while ago", supporting)
    }

    @Test
    fun `UNKNOWN freshness shows low confidence label`() {
        val (label, supporting) = getPriceConfidence(DataFreshness.UNKNOWN)

        assertEquals("Price unverified", label)
        assertEquals("Confirm before ordering", supporting)
    }

    // ==================== Trust Level Ordering Tests ====================

    @Test
    fun `freshness levels have logical ordering`() {
        // LIVE is most trusted, UNKNOWN is least trusted
        val orderedValues = listOf(
            DataFreshness.LIVE,
            DataFreshness.RECENT,
            DataFreshness.CACHED,
            DataFreshness.UNKNOWN
        )

        assertEquals(orderedValues, DataFreshness.values().toList())
    }

    @Test
    fun `LIVE and RECENT are considered verified`() {
        val verifiedStates = listOf(DataFreshness.LIVE, DataFreshness.RECENT)

        verifiedStates.forEach { freshness ->
            val (label, _) = getPriceConfidence(freshness)
            assertEquals("Price verified", label)
        }
    }

    @Test
    fun `CACHED and UNKNOWN are not considered verified`() {
        val unverifiedStates = listOf(DataFreshness.CACHED, DataFreshness.UNKNOWN)

        unverifiedStates.forEach { freshness ->
            val (label, _) = getPriceConfidence(freshness)
            assertNotEquals("Price verified", label)
        }
    }
}

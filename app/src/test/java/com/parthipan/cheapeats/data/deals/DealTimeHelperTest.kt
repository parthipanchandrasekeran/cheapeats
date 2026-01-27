package com.parthipan.cheapeats.data.deals

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for DealTimeHelper.
 */
class DealTimeHelperTest {

    // ==================== Day Bitmask Tests ====================

    @Test
    fun `ALL_DAYS includes all days`() {
        assertEquals(127, DealTimeHelper.ALL_DAYS)
    }

    @Test
    fun `WEEKDAYS equals 31`() {
        assertEquals(31, DealTimeHelper.WEEKDAYS)
    }

    @Test
    fun `WEEKENDS equals 96`() {
        assertEquals(96, DealTimeHelper.WEEKENDS)
    }

    @Test
    fun `createDaysMask creates correct bitmask`() {
        val tuesdayThursday = DealTimeHelper.createDaysMask(
            DealTimeHelper.TUESDAY,
            DealTimeHelper.THURSDAY
        )
        assertEquals(10, tuesdayThursday)
    }

    // ==================== Deal Active Tests ====================

    @Test
    fun `deal with ALL_DAYS is active any day`() {
        val deal = createDeal(validDays = DealTimeHelper.ALL_DAYS)
        assertTrue(DealTimeHelper.isDealActiveNow(deal))
    }

    @Test
    fun `deal with validDays = 0 is active any day`() {
        val deal = createDeal(validDays = 0)
        assertTrue(DealTimeHelper.isDealActiveNow(deal))
    }

    @Test
    fun `expired deal is not active`() {
        val deal = createDeal(
            validUntil = System.currentTimeMillis() - 3600000 // 1 hour ago
        )
        assertFalse(DealTimeHelper.isDealActiveNow(deal))
    }

    @Test
    fun `future deal is not active`() {
        val deal = createDeal(
            validFrom = System.currentTimeMillis() + 3600000 // 1 hour from now
        )
        assertFalse(DealTimeHelper.isDealActiveNow(deal))
    }

    // ==================== Valid Days Text Tests ====================

    @Test
    fun `getValidDaysText returns Every day for ALL_DAYS`() {
        assertEquals("Every day", DealTimeHelper.getValidDaysText(DealTimeHelper.ALL_DAYS))
    }

    @Test
    fun `getValidDaysText returns Weekdays for WEEKDAYS`() {
        assertEquals("Weekdays", DealTimeHelper.getValidDaysText(DealTimeHelper.WEEKDAYS))
    }

    @Test
    fun `getValidDaysText returns Weekends for WEEKENDS`() {
        assertEquals("Weekends", DealTimeHelper.getValidDaysText(DealTimeHelper.WEEKENDS))
    }

    @Test
    fun `getValidDaysText returns single day correctly`() {
        assertEquals("Mon", DealTimeHelper.getValidDaysText(DealTimeHelper.MONDAY))
        assertEquals("Tue", DealTimeHelper.getValidDaysText(DealTimeHelper.TUESDAY))
    }

    @Test
    fun `getValidDaysText returns multiple days correctly`() {
        val monWedFri = DealTimeHelper.createDaysMask(
            DealTimeHelper.MONDAY,
            DealTimeHelper.WEDNESDAY,
            DealTimeHelper.FRIDAY
        )
        assertEquals("Mon, Wed, Fri", DealTimeHelper.getValidDaysText(monWedFri))
    }

    // ==================== Time Remaining Tests ====================

    @Test
    fun `getTimeRemainingText returns null for deal without time constraints`() {
        val deal = createDeal()
        assertNull(DealTimeHelper.getTimeRemainingText(deal))
    }

    @Test
    fun `getTimeRemainingText returns hours remaining for expiring deal`() {
        val deal = createDeal(
            validUntil = System.currentTimeMillis() + 2 * 3600000 // 2 hours from now
        )
        val text = DealTimeHelper.getTimeRemainingText(deal)
        assertNotNull(text)
        assertTrue(text!!.contains("hr") || text.contains("hour"))
    }

    @Test
    fun `getTimeRemainingText returns tomorrow for deal expiring in 30 hours`() {
        val deal = createDeal(
            validUntil = System.currentTimeMillis() + 30 * 3600000 // 30 hours from now
        )
        val text = DealTimeHelper.getTimeRemainingText(deal)
        assertEquals("Ends tomorrow", text)
    }

    @Test
    fun `getTimeRemainingText returns null for expired deal`() {
        val deal = createDeal(
            validUntil = System.currentTimeMillis() - 1000 // Already expired
        )
        assertNull(DealTimeHelper.getTimeRemainingText(deal))
    }

    // ==================== Helper Functions ====================

    private fun createDeal(
        validDays: Int = DealTimeHelper.ALL_DAYS,
        startTime: String? = null,
        endTime: String? = null,
        validFrom: Long? = null,
        validUntil: Long? = null
    ) = Deal(
        id = "test",
        restaurantId = "rest1",
        restaurantName = "Test Restaurant",
        title = "Test Deal",
        description = null,
        originalPrice = 15f,
        dealPrice = 10f,
        dealType = DealType.DAILY_SPECIAL,
        source = DealSource.OFFICIAL,
        validDays = validDays,
        startTime = startTime,
        endTime = endTime,
        validFrom = validFrom,
        validUntil = validUntil
    )
}

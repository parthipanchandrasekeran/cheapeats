package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TodaysSpecialSummary computed properties.
 */
class TodaysSpecialSummaryTest {

    private fun createSummary(
        specialId: Long = 1,
        restaurantId: Long = 1,
        restaurantName: String = "Test Restaurant",
        specialName: String = "Test Special",
        description: String = "Test description",
        specialPrice: Float = 10.00f,
        originalPrice: Float? = null,
        category: SpecialCategory = SpecialCategory.FOOD,
        startTime: String? = null,
        endTime: String? = null,
        restaurantCuisine: String = "Italian",
        restaurantAddress: String = "123 Test St",
        nearTTC: Boolean = false
    ) = TodaysSpecialSummary(
        specialId = specialId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        specialName = specialName,
        description = description,
        specialPrice = specialPrice,
        originalPrice = originalPrice,
        category = category,
        startTime = startTime,
        endTime = endTime,
        restaurantCuisine = restaurantCuisine,
        restaurantAddress = restaurantAddress,
        nearTTC = nearTTC
    )

    // ==================== savings Tests ====================

    @Test
    fun `savings returns null when originalPrice is null`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = null)
        assertNull(summary.savings)
    }

    @Test
    fun `savings calculates correctly when originalPrice is provided`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = 15.00f)
        assertEquals(5.00f, summary.savings!!, 0.001f)
    }

    @Test
    fun `savings returns zero when prices are equal`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = 10.00f)
        assertEquals(0.00f, summary.savings!!, 0.001f)
    }

    @Test
    fun `savings handles large discounts`() {
        val summary = createSummary(specialPrice = 5.00f, originalPrice = 25.00f)
        assertEquals(20.00f, summary.savings!!, 0.001f)
    }

    // ==================== discountPercentage Tests ====================

    @Test
    fun `discountPercentage returns null when originalPrice is null`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = null)
        assertNull(summary.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 50 percent correctly`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = 20.00f)
        assertEquals(50, summary.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 20 percent correctly`() {
        val summary = createSummary(specialPrice = 8.00f, originalPrice = 10.00f)
        assertEquals(20, summary.discountPercentage)
    }

    @Test
    fun `discountPercentage returns 0 when prices are equal`() {
        val summary = createSummary(specialPrice = 10.00f, originalPrice = 10.00f)
        assertEquals(0, summary.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 75 percent correctly`() {
        val summary = createSummary(specialPrice = 5.00f, originalPrice = 20.00f)
        assertEquals(75, summary.discountPercentage)
    }

    // ==================== timeRangeDisplay Tests ====================

    @Test
    fun `timeRangeDisplay returns All Day when both times are null`() {
        val summary = createSummary(startTime = null, endTime = null)
        assertEquals("All Day", summary.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns range when both times are set`() {
        val summary = createSummary(startTime = "11:00", endTime = "15:00")
        assertEquals("11:00 - 15:00", summary.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns From startTime when only start is set`() {
        val summary = createSummary(startTime = "11:00", endTime = null)
        assertEquals("From 11:00", summary.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns Until endTime when only end is set`() {
        val summary = createSummary(startTime = null, endTime = "15:00")
        assertEquals("Until 15:00", summary.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay handles morning hours`() {
        val summary = createSummary(startTime = "06:00", endTime = "10:00")
        assertEquals("06:00 - 10:00", summary.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay handles evening hours`() {
        val summary = createSummary(startTime = "18:00", endTime = "23:00")
        assertEquals("18:00 - 23:00", summary.timeRangeDisplay)
    }

    // ==================== Data Class Tests ====================

    @Test
    fun `summary contains all restaurant info`() {
        val summary = createSummary(
            restaurantName = "Test Restaurant",
            restaurantCuisine = "Italian",
            restaurantAddress = "123 Test St",
            nearTTC = true
        )

        assertEquals("Test Restaurant", summary.restaurantName)
        assertEquals("Italian", summary.restaurantCuisine)
        assertEquals("123 Test St", summary.restaurantAddress)
        assertTrue(summary.nearTTC)
    }

    @Test
    fun `summary contains all special info`() {
        val summary = createSummary(
            specialName = "Lunch Special",
            description = "Great deal!",
            specialPrice = 9.99f,
            category = SpecialCategory.LUNCH
        )

        assertEquals("Lunch Special", summary.specialName)
        assertEquals("Great deal!", summary.description)
        assertEquals(9.99f, summary.specialPrice, 0.001f)
        assertEquals(SpecialCategory.LUNCH, summary.category)
    }

    @Test
    fun `summaries with same data are equal`() {
        val summary1 = createSummary(specialId = 1, restaurantId = 1)
        val summary2 = createSummary(specialId = 1, restaurantId = 1)
        assertEquals(summary1, summary2)
    }

    @Test
    fun `summaries with different ids are not equal`() {
        val summary1 = createSummary(specialId = 1)
        val summary2 = createSummary(specialId = 2)
        assertNotEquals(summary1, summary2)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `handles zero special price`() {
        val summary = createSummary(specialPrice = 0f, originalPrice = 10.00f)
        assertEquals(10.00f, summary.savings!!, 0.001f)
        assertEquals(100, summary.discountPercentage)
    }

    @Test
    fun `handles all categories`() {
        SpecialCategory.entries.forEach { category ->
            val summary = createSummary(category = category)
            assertEquals(category, summary.category)
        }
    }
}

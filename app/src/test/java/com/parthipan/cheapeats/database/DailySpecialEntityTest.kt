package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DailySpecialEntity computed properties.
 */
class DailySpecialEntityTest {

    private fun createSpecial(
        id: Long = 1,
        restaurantId: Long = 1,
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        specialName: String = "Test Special",
        description: String = "Test description",
        specialPrice: Float = 10.00f,
        originalPrice: Float? = null,
        category: SpecialCategory = SpecialCategory.FOOD,
        isActive: Boolean = true,
        startTime: String? = null,
        endTime: String? = null,
        termsConditions: String? = null,
        imageUrl: String? = null
    ) = DailySpecialEntity(
        id = id,
        restaurantId = restaurantId,
        dayOfWeek = dayOfWeek,
        specialName = specialName,
        description = description,
        specialPrice = specialPrice,
        originalPrice = originalPrice,
        category = category,
        isActive = isActive,
        startTime = startTime,
        endTime = endTime,
        termsConditions = termsConditions,
        imageUrl = imageUrl
    )

    // ==================== savings Tests ====================

    @Test
    fun `savings returns null when originalPrice is null`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = null)
        assertNull(special.savings)
    }

    @Test
    fun `savings calculates correctly when originalPrice is provided`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = 15.00f)
        assertEquals(5.00f, special.savings!!, 0.001f)
    }

    @Test
    fun `savings returns zero when prices are equal`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = 10.00f)
        assertEquals(0.00f, special.savings!!, 0.001f)
    }

    @Test
    fun `savings can be negative when special price is higher`() {
        val special = createSpecial(specialPrice = 15.00f, originalPrice = 10.00f)
        assertEquals(-5.00f, special.savings!!, 0.001f)
    }

    @Test
    fun `savings handles decimal values`() {
        val special = createSpecial(specialPrice = 8.99f, originalPrice = 12.99f)
        assertEquals(4.00f, special.savings!!, 0.01f)
    }

    // ==================== discountPercentage Tests ====================

    @Test
    fun `discountPercentage returns null when originalPrice is null`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = null)
        assertNull(special.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 50 percent correctly`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = 20.00f)
        assertEquals(50, special.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 33 percent correctly`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = 15.00f)
        assertEquals(33, special.discountPercentage)
    }

    @Test
    fun `discountPercentage returns 0 when prices are equal`() {
        val special = createSpecial(specialPrice = 10.00f, originalPrice = 10.00f)
        assertEquals(0, special.discountPercentage)
    }

    @Test
    fun `discountPercentage calculates 25 percent correctly`() {
        val special = createSpecial(specialPrice = 15.00f, originalPrice = 20.00f)
        assertEquals(25, special.discountPercentage)
    }

    @Test
    fun `discountPercentage truncates to integer`() {
        // 9.99 / 14.99 = ~33.3%
        val special = createSpecial(specialPrice = 9.99f, originalPrice = 14.99f)
        assertTrue(special.discountPercentage!! in 33..34)
    }

    // ==================== isAllDay Tests ====================

    @Test
    fun `isAllDay returns true when both times are null`() {
        val special = createSpecial(startTime = null, endTime = null)
        assertTrue(special.isAllDay)
    }

    @Test
    fun `isAllDay returns false when startTime is set`() {
        val special = createSpecial(startTime = "11:00", endTime = null)
        assertFalse(special.isAllDay)
    }

    @Test
    fun `isAllDay returns false when endTime is set`() {
        val special = createSpecial(startTime = null, endTime = "15:00")
        assertFalse(special.isAllDay)
    }

    @Test
    fun `isAllDay returns false when both times are set`() {
        val special = createSpecial(startTime = "11:00", endTime = "15:00")
        assertFalse(special.isAllDay)
    }

    // ==================== timeRangeDisplay Tests ====================

    @Test
    fun `timeRangeDisplay returns All Day when both times are null`() {
        val special = createSpecial(startTime = null, endTime = null)
        assertEquals("All Day", special.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns range when both times are set`() {
        val special = createSpecial(startTime = "11:00", endTime = "15:00")
        assertEquals("11:00 - 15:00", special.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns From startTime when only start is set`() {
        val special = createSpecial(startTime = "11:00", endTime = null)
        assertEquals("From 11:00", special.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay returns Until endTime when only end is set`() {
        val special = createSpecial(startTime = null, endTime = "15:00")
        assertEquals("Until 15:00", special.timeRangeDisplay)
    }

    @Test
    fun `timeRangeDisplay handles different time formats`() {
        val special = createSpecial(startTime = "09:30", endTime = "21:00")
        assertEquals("09:30 - 21:00", special.timeRangeDisplay)
    }

    // ==================== Default Values Tests ====================

    @Test
    fun `default category is FOOD`() {
        val special = createSpecial()
        assertEquals(SpecialCategory.FOOD, special.category)
    }

    @Test
    fun `default isActive is true`() {
        val special = createSpecial()
        assertTrue(special.isActive)
    }

    @Test
    fun `default originalPrice is null`() {
        val special = createSpecial()
        assertNull(special.originalPrice)
    }

    @Test
    fun `createdAt is set automatically`() {
        val beforeCreation = System.currentTimeMillis()
        val special = createSpecial()
        val afterCreation = System.currentTimeMillis()

        assertTrue(special.createdAt >= beforeCreation)
        assertTrue(special.createdAt <= afterCreation)
    }

    // ==================== Data Class Tests ====================

    @Test
    fun `specials with same data are equal`() {
        val special1 = DailySpecialEntity(
            id = 1,
            restaurantId = 1,
            dayOfWeek = DayOfWeek.MONDAY,
            specialName = "Test",
            description = "Desc",
            specialPrice = 10.00f,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val special2 = DailySpecialEntity(
            id = 1,
            restaurantId = 1,
            dayOfWeek = DayOfWeek.MONDAY,
            specialName = "Test",
            description = "Desc",
            specialPrice = 10.00f,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        assertEquals(special1, special2)
    }

    @Test
    fun `copy works correctly`() {
        val original = createSpecial(specialName = "Original")
        val copy = original.copy(specialName = "Copy")
        assertEquals("Copy", copy.specialName)
        assertEquals(original.specialPrice, copy.specialPrice, 0.001f)
    }
}

/**
 * Unit tests for SpecialCategory enum.
 */
class SpecialCategoryTest {

    @Test
    fun `SpecialCategory has 9 entries`() {
        assertEquals(9, SpecialCategory.entries.size)
    }

    @Test
    fun `FOOD has correct display name`() {
        assertEquals("Food", SpecialCategory.FOOD.displayName)
    }

    @Test
    fun `DRINK has correct display name`() {
        assertEquals("Drink", SpecialCategory.DRINK.displayName)
    }

    @Test
    fun `COMBO has correct display name`() {
        assertEquals("Combo", SpecialCategory.COMBO.displayName)
    }

    @Test
    fun `APPETIZER has correct display name`() {
        assertEquals("Appetizer", SpecialCategory.APPETIZER.displayName)
    }

    @Test
    fun `DESSERT has correct display name`() {
        assertEquals("Dessert", SpecialCategory.DESSERT.displayName)
    }

    @Test
    fun `HAPPY_HOUR has correct display name`() {
        assertEquals("Happy Hour", SpecialCategory.HAPPY_HOUR.displayName)
    }

    @Test
    fun `BRUNCH has correct display name`() {
        assertEquals("Brunch", SpecialCategory.BRUNCH.displayName)
    }

    @Test
    fun `LUNCH has correct display name`() {
        assertEquals("Lunch", SpecialCategory.LUNCH.displayName)
    }

    @Test
    fun `DINNER has correct display name`() {
        assertEquals("Dinner", SpecialCategory.DINNER.displayName)
    }

    @Test
    fun `all categories have unique display names`() {
        val displayNames = SpecialCategory.entries.map { it.displayName }
        assertEquals(SpecialCategory.entries.size, displayNames.toSet().size)
    }
}

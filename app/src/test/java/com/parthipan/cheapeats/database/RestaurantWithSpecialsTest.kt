package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RestaurantWithSpecials relationship class.
 * Note: Tests use mocked/stubbed data since we can't easily create
 * Room relationship objects in unit tests without the database.
 */
class RestaurantWithSpecialsTest {

    private fun createRestaurantEntity(
        id: Long = 1,
        placeId: String = "place_1",
        name: String = "Test Restaurant"
    ) = RestaurantEntity(
        id = id,
        placeId = placeId,
        name = name,
        cuisine = "Italian",
        priceLevel = 2,
        rating = 4.5f,
        imageUrl = null,
        address = "123 Test St",
        latitude = 43.7615,
        longitude = -79.3456,
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 15.00f,
        isFavorite = false
    )

    private fun createDailySpecial(
        id: Long = 1,
        restaurantId: Long = 1,
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        isActive: Boolean = true,
        specialName: String = "Test Special"
    ) = DailySpecialEntity(
        id = id,
        restaurantId = restaurantId,
        dayOfWeek = dayOfWeek,
        specialName = specialName,
        description = "Test description",
        specialPrice = 10.00f,
        isActive = isActive
    )

    // ==================== specialsForDay Tests ====================

    @Test
    fun `specialsForDay returns specials for specified day`() {
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = DayOfWeek.MONDAY),
            createDailySpecial(id = 2, dayOfWeek = DayOfWeek.TUESDAY),
            createDailySpecial(id = 3, dayOfWeek = DayOfWeek.MONDAY)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val mondaySpecials = restaurantWithSpecials.specialsForDay(DayOfWeek.MONDAY)

        assertEquals(2, mondaySpecials.size)
        assertTrue(mondaySpecials.all { it.dayOfWeek == DayOfWeek.MONDAY })
    }

    @Test
    fun `specialsForDay returns empty list when no specials for day`() {
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = DayOfWeek.MONDAY),
            createDailySpecial(id = 2, dayOfWeek = DayOfWeek.TUESDAY)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val wednesdaySpecials = restaurantWithSpecials.specialsForDay(DayOfWeek.WEDNESDAY)

        assertTrue(wednesdaySpecials.isEmpty())
    }

    @Test
    fun `specialsForDay only returns active specials`() {
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = DayOfWeek.MONDAY, isActive = true),
            createDailySpecial(id = 2, dayOfWeek = DayOfWeek.MONDAY, isActive = false),
            createDailySpecial(id = 3, dayOfWeek = DayOfWeek.MONDAY, isActive = true)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val mondaySpecials = restaurantWithSpecials.specialsForDay(DayOfWeek.MONDAY)

        assertEquals(2, mondaySpecials.size)
        assertTrue(mondaySpecials.all { it.isActive })
    }

    @Test
    fun `specialsForDay filters by both day and active status`() {
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = DayOfWeek.MONDAY, isActive = true),
            createDailySpecial(id = 2, dayOfWeek = DayOfWeek.MONDAY, isActive = false),
            createDailySpecial(id = 3, dayOfWeek = DayOfWeek.TUESDAY, isActive = true),
            createDailySpecial(id = 4, dayOfWeek = DayOfWeek.TUESDAY, isActive = false)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val mondaySpecials = restaurantWithSpecials.specialsForDay(DayOfWeek.MONDAY)
        val tuesdaySpecials = restaurantWithSpecials.specialsForDay(DayOfWeek.TUESDAY)

        assertEquals(1, mondaySpecials.size)
        assertEquals(1, tuesdaySpecials.size)
    }

    // ==================== todaysSpecials Tests ====================

    @Test
    fun `todaysSpecials returns specials for current day`() {
        val today = DayOfWeek.today()
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = today, isActive = true),
            createDailySpecial(id = 2, dayOfWeek = today, isActive = true)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val todaysSpecials = restaurantWithSpecials.todaysSpecials()

        assertEquals(2, todaysSpecials.size)
    }

    @Test
    fun `todaysSpecials returns empty when no specials for today`() {
        // Get a day that is not today
        val notToday = DayOfWeek.entries.first { it != DayOfWeek.today() }
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = notToday, isActive = true)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        val todaysSpecials = restaurantWithSpecials.todaysSpecials()

        assertTrue(todaysSpecials.isEmpty())
    }

    // ==================== hasSpecialsToday Tests ====================

    @Test
    fun `hasSpecialsToday returns true when specials exist for today`() {
        val today = DayOfWeek.today()
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = today, isActive = true)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        assertTrue(restaurantWithSpecials.hasSpecialsToday())
    }

    @Test
    fun `hasSpecialsToday returns false when no specials for today`() {
        val notToday = DayOfWeek.entries.first { it != DayOfWeek.today() }
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = notToday, isActive = true)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        assertFalse(restaurantWithSpecials.hasSpecialsToday())
    }

    @Test
    fun `hasSpecialsToday returns false when todays specials are inactive`() {
        val today = DayOfWeek.today()
        val restaurant = createRestaurantEntity()
        val specials = listOf(
            createDailySpecial(id = 1, dayOfWeek = today, isActive = false)
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        assertFalse(restaurantWithSpecials.hasSpecialsToday())
    }

    @Test
    fun `hasSpecialsToday returns false when specials list is empty`() {
        val restaurant = createRestaurantEntity()
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, emptyList())

        assertFalse(restaurantWithSpecials.hasSpecialsToday())
    }

    // ==================== Edge Cases ====================

    @Test
    fun `handles restaurant with many specials`() {
        val restaurant = createRestaurantEntity()
        val specials = DayOfWeek.entries.flatMapIndexed { index, day ->
            listOf(
                createDailySpecial(id = (index * 2 + 1).toLong(), dayOfWeek = day, specialName = "Special 1"),
                createDailySpecial(id = (index * 2 + 2).toLong(), dayOfWeek = day, specialName = "Special 2")
            )
        }
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, specials)

        assertEquals(14, restaurantWithSpecials.specials.size) // 7 days * 2 specials
        DayOfWeek.entries.forEach { day ->
            assertEquals(2, restaurantWithSpecials.specialsForDay(day).size)
        }
    }

    @Test
    fun `restaurant data is accessible`() {
        val restaurant = createRestaurantEntity(
            name = "Great Restaurant",
            id = 42
        )
        val restaurantWithSpecials = RestaurantWithSpecials(restaurant, emptyList())

        assertEquals("Great Restaurant", restaurantWithSpecials.restaurant.name)
        assertEquals(42L, restaurantWithSpecials.restaurant.id)
    }
}

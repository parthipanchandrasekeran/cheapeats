package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for FilterContract hard filter enforcement.
 */
class FilterContractTest {

    // ==================== Open Now Hard Filter ====================

    @Test
    fun `mustBeOpen filter excludes closed restaurants`() {
        val restaurant = createRestaurant(isOpenNow = false)
        val filters = FilterContract.HardFilters(mustBeOpen = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `mustBeOpen filter excludes unknown open status`() {
        val restaurant = createRestaurant(isOpenNow = null)
        val filters = FilterContract.HardFilters(mustBeOpen = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `mustBeOpen filter includes open restaurants`() {
        val restaurant = createRestaurant(isOpenNow = true)
        val filters = FilterContract.HardFilters(mustBeOpen = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNotNull(result)
        assertEquals(restaurant, result)
    }

    // ==================== Strict Under $15 Hard Filter ====================

    @Test
    fun `strictUnder15 filter excludes verified price over 15`() {
        val restaurant = createRestaurant(
            averagePrice = 18f,
            priceSource = PriceSource.API_VERIFIED
        )
        val filters = FilterContract.HardFilters(strictUnder15 = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `strictUnder15 filter excludes estimated prices`() {
        val restaurant = createRestaurant(
            averagePrice = 12f,
            priceSource = PriceSource.ESTIMATED
        )
        val filters = FilterContract.HardFilters(strictUnder15 = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `strictUnder15 filter includes verified price under 15`() {
        val restaurant = createRestaurant(
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED
        )
        val filters = FilterContract.HardFilters(strictUnder15 = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNotNull(result)
    }

    // ==================== Near TTC Hard Filter ====================

    @Test
    fun `mustBeNearTTC filter excludes restaurants not near TTC`() {
        val restaurant = createRestaurant(nearTTC = false)
        val filters = FilterContract.HardFilters(mustBeNearTTC = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `mustBeNearTTC filter includes restaurants near TTC`() {
        val restaurant = createRestaurant(nearTTC = true)
        val filters = FilterContract.HardFilters(mustBeNearTTC = true)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNotNull(result)
    }

    // ==================== Max Walk Time Hard Filter ====================

    @Test
    fun `maxWalkMinutes filter excludes restaurants beyond walk time`() {
        val restaurant = createRestaurant(ttcWalkMinutes = 15)
        val filters = FilterContract.HardFilters(maxWalkMinutes = 10)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    @Test
    fun `maxWalkMinutes filter includes restaurants within walk time`() {
        val restaurant = createRestaurant(ttcWalkMinutes = 5)
        val filters = FilterContract.HardFilters(maxWalkMinutes = 10)

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNotNull(result)
    }

    // ==================== Combined Filters ====================

    @Test
    fun `multiple hard filters are applied with AND logic`() {
        val restaurant = createRestaurant(
            isOpenNow = true,
            nearTTC = true,
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED
        )
        val filters = FilterContract.HardFilters(
            mustBeOpen = true,
            mustBeNearTTC = true,
            strictUnder15 = true
        )

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNotNull(result)
    }

    @Test
    fun `fails if any hard filter is not met`() {
        val restaurant = createRestaurant(
            isOpenNow = true,
            nearTTC = false,  // This fails
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED
        )
        val filters = FilterContract.HardFilters(
            mustBeOpen = true,
            mustBeNearTTC = true,
            strictUnder15 = true
        )

        val result = FilterContract.validateHardFilters(restaurant, filters)

        assertNull(result)
    }

    // ==================== Batch Filter ====================

    @Test
    fun `applyHardFilters filters list correctly`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isOpenNow = true),
            createRestaurant(id = "2", isOpenNow = false),
            createRestaurant(id = "3", isOpenNow = true),
            createRestaurant(id = "4", isOpenNow = null)
        )
        val filters = FilterContract.HardFilters(mustBeOpen = true)

        val result = FilterContract.applyHardFilters(restaurants, filters)

        assertEquals(2, result.size)
        assertTrue(result.all { it.isOpenNow == true })
    }

    // ==================== Helper Functions ====================

    private fun createRestaurant(
        id: String = "1",
        isOpenNow: Boolean? = true,
        nearTTC: Boolean = true,
        averagePrice: Float? = 12f,
        priceSource: PriceSource = PriceSource.API_VERIFIED,
        ttcWalkMinutes: Int? = 5
    ) = Restaurant(
        id = id,
        name = "Test Restaurant",
        cuisine = "Italian",
        priceLevel = 1,
        rating = 4.0f,
        distance = 0.5f,
        imageUrl = null,
        address = "123 Test St",
        location = LatLng(43.6453, -79.3806),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        priceSource = priceSource,
        isOpenNow = isOpenNow,
        dataFreshness = DataFreshness.LIVE,
        ttcWalkMinutes = ttcWalkMinutes
    )
}

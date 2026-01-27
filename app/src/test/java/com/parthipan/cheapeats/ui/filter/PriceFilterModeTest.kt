package com.parthipan.cheapeats.ui.filter

import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.DataFreshness
import com.parthipan.cheapeats.data.PriceSource
import com.parthipan.cheapeats.data.Restaurant
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PriceFilterMode and price filtering logic.
 */
class PriceFilterModeTest {

    private fun createRestaurant(
        id: String = "1",
        averagePrice: Float? = 12.0f,
        priceLevel: Int = 1,
        priceSource: PriceSource = PriceSource.API_VERIFIED
    ) = Restaurant(
        id = id,
        name = "Test Restaurant",
        cuisine = "Italian",
        priceLevel = priceLevel,
        rating = 4.0f,
        distance = 0.5f,
        imageUrl = null,
        address = "123 Test St",
        location = LatLng(43.6453, -79.3806),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = averagePrice,
        priceSource = priceSource,
        isOpenNow = true,
        dataFreshness = DataFreshness.LIVE
    )

    // ==================== Strict Mode Tests ====================

    @Test
    fun `strict mode includes verified price under 15`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f, priceSource = PriceSource.API_VERIFIED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
    }

    @Test
    fun `strict mode excludes verified price over 15`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 18.0f, priceSource = PriceSource.API_VERIFIED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(0, filtered.size)
    }

    @Test
    fun `strict mode excludes estimated prices`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f, priceSource = PriceSource.ESTIMATED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(0, filtered.size)
    }

    @Test
    fun `strict mode includes unknown source with price under 15`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 10.0f, priceSource = PriceSource.UNKNOWN)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
    }

    @Test
    fun `strict mode uses priceLevel when averagePrice is null`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = null, priceLevel = 1),
            createRestaurant(id = "2", averagePrice = null, priceLevel = 2)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
        assertEquals("1", filtered[0].id)
    }

    // ==================== Flexible Mode Tests ====================

    @Test
    fun `flexible mode includes verified price under 15`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f, priceSource = PriceSource.API_VERIFIED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
    }

    @Test
    fun `flexible mode includes estimated price under 17`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 16.0f, priceSource = PriceSource.ESTIMATED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
    }

    @Test
    fun `flexible mode includes price at exactly 17`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 17.0f, priceSource = PriceSource.ESTIMATED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
    }

    @Test
    fun `flexible mode excludes price over 17`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 18.0f, priceSource = PriceSource.ESTIMATED)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(0, filtered.size)
    }

    @Test
    fun `flexible mode uses priceLevel when averagePrice is null`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = null, priceLevel = 1),
            createRestaurant(id = "2", averagePrice = null, priceLevel = 3)
        )
        val state = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val filtered = FilterViewModel.applyFilters(restaurants, state)

        assertEquals(1, filtered.size)
        assertEquals("1", filtered[0].id)
    }

    // ==================== Mode Comparison Tests ====================

    @Test
    fun `flexible mode returns more results than strict for estimated prices`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f, priceSource = PriceSource.API_VERIFIED),
            createRestaurant(id = "2", averagePrice = 14.0f, priceSource = PriceSource.ESTIMATED),
            createRestaurant(id = "3", averagePrice = 16.0f, priceSource = PriceSource.ESTIMATED)
        )

        val strictState = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.STRICT)
        val flexibleState = FilterState(isUnder15Active = true, priceFilterMode = PriceFilterMode.FLEXIBLE)

        val strictFiltered = FilterViewModel.applyFilters(restaurants, strictState)
        val flexibleFiltered = FilterViewModel.applyFilters(restaurants, flexibleState)

        assertEquals(1, strictFiltered.size) // Only verified under $15
        assertEquals(3, flexibleFiltered.size) // All three qualify
    }

    // ==================== Restaurant Property Tests ====================

    @Test
    fun `isVerifiedUnder15 returns true for verified price under 15`() {
        val restaurant = createRestaurant(averagePrice = 12.0f, priceSource = PriceSource.API_VERIFIED)
        assertTrue(restaurant.isVerifiedUnder15)
    }

    @Test
    fun `isVerifiedUnder15 returns false for estimated price under 15`() {
        val restaurant = createRestaurant(averagePrice = 12.0f, priceSource = PriceSource.ESTIMATED)
        assertFalse(restaurant.isVerifiedUnder15)
    }

    @Test
    fun `isVerifiedUnder15 returns false for verified price over 15`() {
        val restaurant = createRestaurant(averagePrice = 18.0f, priceSource = PriceSource.API_VERIFIED)
        assertFalse(restaurant.isVerifiedUnder15)
    }

    @Test
    fun `isFlexiblyUnder15 returns true for price at 17`() {
        val restaurant = createRestaurant(averagePrice = 17.0f)
        assertTrue(restaurant.isFlexiblyUnder15)
    }

    @Test
    fun `isFlexiblyUnder15 returns false for price over 17`() {
        val restaurant = createRestaurant(averagePrice = 18.0f)
        assertFalse(restaurant.isFlexiblyUnder15)
    }

    @Test
    fun `isFlexiblyUnder15 uses priceLevel when price is null`() {
        val cheap = createRestaurant(averagePrice = null, priceLevel = 1)
        val expensive = createRestaurant(averagePrice = null, priceLevel = 3)

        assertTrue(cheap.isFlexiblyUnder15)
        assertFalse(expensive.isFlexiblyUnder15)
    }

    @Test
    fun `priceConfidenceLabel returns correct labels`() {
        val verified = createRestaurant(priceSource = PriceSource.API_VERIFIED)
        val estimated = createRestaurant(priceSource = PriceSource.ESTIMATED)
        val unknown = createRestaurant(priceSource = PriceSource.UNKNOWN)

        assertEquals("Verified", verified.priceConfidenceLabel)
        assertEquals("Estimated", estimated.priceConfidenceLabel)
        assertEquals("", unknown.priceConfidenceLabel)
    }
}

package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.ui.filter.FilterState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ReasonGenerator.
 */
class ReasonGeneratorTest {

    // ==================== Generate Reasons Tests ====================

    @Test
    fun `open restaurant gets OPEN_NOW reason`() {
        val restaurant = createRestaurant(isOpenNow = true)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.OPEN_NOW))
    }

    @Test
    fun `closed restaurant does not get OPEN_NOW reason`() {
        val restaurant = createRestaurant(isOpenNow = false)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertFalse(reasons.contains(RecommendationReason.OPEN_NOW))
    }

    @Test
    fun `verified under 15 restaurant gets VERIFIED_UNDER_15 reason`() {
        val restaurant = createRestaurant(
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED
        )

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.VERIFIED_UNDER_15))
    }

    @Test
    fun `estimated under 15 restaurant gets ESTIMATED_UNDER_15 reason`() {
        val restaurant = createRestaurant(
            averagePrice = 14f,
            priceSource = PriceSource.ESTIMATED
        )

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.ESTIMATED_UNDER_15))
    }

    @Test
    fun `restaurant near TTC with short walk gets NEAR_TTC reason`() {
        val restaurant = createRestaurant(nearTTC = true, ttcWalkMinutes = 3)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.NEAR_TTC))
    }

    @Test
    fun `restaurant far from TTC does not get NEAR_TTC reason`() {
        val restaurant = createRestaurant(nearTTC = true, ttcWalkMinutes = 10)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertFalse(reasons.contains(RecommendationReason.NEAR_TTC))
    }

    @Test
    fun `high rated restaurant gets HIGH_RATING reason`() {
        val restaurant = createRestaurant(rating = 4.5f)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.HIGH_RATING))
    }

    @Test
    fun `low rated restaurant does not get HIGH_RATING reason`() {
        val restaurant = createRestaurant(rating = 3.5f)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertFalse(reasons.contains(RecommendationReason.HIGH_RATING))
    }

    @Test
    fun `restaurant matching search query gets QUERY_MATCH reason`() {
        val restaurant = createRestaurant(name = "Pizza Palace")

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState(), searchQuery = "pizza")

        assertTrue(reasons.contains(RecommendationReason.QUERY_MATCH))
    }

    @Test
    fun `restaurant not matching search query does not get QUERY_MATCH reason`() {
        val restaurant = createRestaurant(name = "Burger Joint")

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState(), searchQuery = "pizza")

        assertFalse(reasons.contains(RecommendationReason.QUERY_MATCH))
    }

    @Test
    fun `restaurant with student discount gets STUDENT_DISCOUNT reason`() {
        val restaurant = createRestaurant(hasStudentDiscount = true)

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.contains(RecommendationReason.STUDENT_DISCOUNT))
    }

    @Test
    fun `reasons are limited to max 4`() {
        val restaurant = createRestaurant(
            isOpenNow = true,
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED,
            nearTTC = true,
            ttcWalkMinutes = 2,
            rating = 4.8f,
            hasStudentDiscount = true
        )

        val reasons = ReasonGenerator.generateReasons(restaurant, FilterState())

        assertTrue(reasons.size <= 4)
    }

    // ==================== Generate Explanation Tests ====================

    @Test
    fun `empty reasons returns default explanation`() {
        val explanation = ReasonGenerator.generateExplanation(emptyList())

        assertEquals("Nearby option", explanation)
    }

    @Test
    fun `single reason generates correct explanation`() {
        val reasons = listOf(RecommendationReason.OPEN_NOW)

        val explanation = ReasonGenerator.generateExplanation(reasons)

        assertEquals("Open now", explanation)
    }

    @Test
    fun `two reasons generates comma-separated explanation`() {
        val reasons = listOf(
            RecommendationReason.VERIFIED_UNDER_15,
            RecommendationReason.NEAR_TTC
        )

        val explanation = ReasonGenerator.generateExplanation(reasons)

        assertTrue(explanation.contains("verified cheap", ignoreCase = true))
        assertTrue(explanation.contains("steps from transit", ignoreCase = true))
    }

    @Test
    fun `explanation only uses first two reasons`() {
        val reasons = listOf(
            RecommendationReason.OPEN_NOW,
            RecommendationReason.VERIFIED_UNDER_15,
            RecommendationReason.NEAR_TTC,
            RecommendationReason.HIGH_RATING
        )

        val explanation = ReasonGenerator.generateExplanation(reasons)

        // Should only contain first two
        assertTrue(explanation.contains("open now", ignoreCase = true))
        assertTrue(explanation.contains("verified cheap", ignoreCase = true))
        assertFalse(explanation.contains("transit", ignoreCase = true))
    }

    // ==================== Rank With Reasons Tests ====================

    @Test
    fun `rankWithReasons creates complete RankedRestaurant`() {
        val restaurant = createRestaurant(
            isOpenNow = true,
            averagePrice = 12f,
            priceSource = PriceSource.API_VERIFIED
        )

        val ranked = ReasonGenerator.rankWithReasons(
            restaurant = restaurant,
            score = 0.85f,
            filterState = FilterState()
        )

        assertEquals(restaurant, ranked.restaurant)
        assertEquals(0.85f, ranked.score)
        assertTrue(ranked.reasons.isNotEmpty())
        assertTrue(ranked.explanation.isNotEmpty())
    }

    // ==================== Helper Functions ====================

    private fun createRestaurant(
        name: String = "Test Restaurant",
        cuisine: String = "Italian",
        isOpenNow: Boolean? = true,
        nearTTC: Boolean = true,
        ttcWalkMinutes: Int? = 5,
        averagePrice: Float? = 12f,
        priceSource: PriceSource = PriceSource.API_VERIFIED,
        rating: Float = 4.0f,
        hasStudentDiscount: Boolean = false
    ) = Restaurant(
        id = "1",
        name = name,
        cuisine = cuisine,
        priceLevel = 1,
        rating = rating,
        distance = 0.5f,
        imageUrl = null,
        address = "123 Test St",
        location = LatLng(43.6453, -79.3806),
        isSponsored = false,
        hasStudentDiscount = hasStudentDiscount,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        priceSource = priceSource,
        isOpenNow = isOpenNow,
        dataFreshness = DataFreshness.LIVE,
        ttcWalkMinutes = ttcWalkMinutes
    )
}

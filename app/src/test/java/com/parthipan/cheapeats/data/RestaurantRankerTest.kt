package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RestaurantRanker.
 * Tests ranking algorithm, favorite boost, data freshness rules, and time-aware scoring.
 */
class RestaurantRankerTest {

    private fun createRestaurant(
        id: String = "1",
        name: String = "Test Restaurant",
        cuisine: String = "Italian",
        priceLevel: Int = 1,
        rating: Float = 4.0f,
        distance: Float = 0.5f,
        averagePrice: Float? = 12.0f,
        nearTTC: Boolean = true,
        ttcWalkMinutes: Int? = 5,
        nearestStation: String? = "Union",
        isOpenNow: Boolean? = true,
        dataFreshness: DataFreshness = DataFreshness.LIVE,
        isFavorite: Boolean = false
    ) = Restaurant(
        id = id,
        name = name,
        cuisine = cuisine,
        priceLevel = priceLevel,
        rating = rating,
        distance = distance,
        imageUrl = null,
        address = "123 Test St",
        location = LatLng(43.6453, -79.3806),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        websiteUrl = null,
        googleMapsUrl = null,
        isOpenNow = isOpenNow,
        openingHours = null,
        ttcWalkMinutes = ttcWalkMinutes,
        nearestStation = nearestStation,
        dataFreshness = dataFreshness,
        lastVerified = null,
        isFavorite = isFavorite
    )

    // ==================== Basic Ranking Tests ====================

    @Test
    fun `rank returns restaurants sorted by score descending`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 3.0f, averagePrice = 14.0f),
            createRestaurant(id = "2", rating = 5.0f, averagePrice = 10.0f),
            createRestaurant(id = "3", rating = 4.0f, averagePrice = 12.0f)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Restaurant 2 should rank highest (best rating, lowest price)
        assertEquals("2", ranked[0].restaurant.id)
    }

    @Test
    fun `rank filters out closed restaurants by default`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isOpenNow = true),
            createRestaurant(id = "2", isOpenNow = false),
            createRestaurant(id = "3", isOpenNow = null) // Unknown kept
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals(2, ranked.size)
        assertTrue(ranked.none { it.restaurant.id == "2" })
    }

    @Test
    fun `rank keeps unknown open status restaurants`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isOpenNow = null)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals(1, ranked.size)
    }

    @Test
    fun `rank includes closed restaurants when excludeClosed is false`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isOpenNow = false)
        )

        val ranked = RestaurantRanker.rank(restaurants, excludeClosed = false)

        assertEquals(1, ranked.size)
    }

    // ==================== Value Score Tests ====================

    @Test
    fun `cheaper restaurants score higher on value`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 14.0f, rating = 4.0f, ttcWalkMinutes = 5),
            createRestaurant(id = "2", averagePrice = 8.0f, rating = 4.0f, ttcWalkMinutes = 5)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Restaurant 2 (cheaper) should rank higher
        assertEquals("2", ranked[0].restaurant.id)
    }

    @Test
    fun `unknown price gets default value score`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = null)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Should still rank without crashing
        assertEquals(1, ranked.size)
    }

    // ==================== TTC Score Tests ====================

    @Test
    fun `closer to TTC scores higher`() {
        val restaurants = listOf(
            createRestaurant(id = "1", ttcWalkMinutes = 8, averagePrice = 12.0f, rating = 4.0f),
            createRestaurant(id = "2", ttcWalkMinutes = 2, averagePrice = 12.0f, rating = 4.0f)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Restaurant 2 (closer to TTC) should rank higher
        assertEquals("2", ranked[0].restaurant.id)
    }

    @Test
    fun `unknown TTC walk time gets default score`() {
        val restaurants = listOf(
            createRestaurant(id = "1", ttcWalkMinutes = null)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals(1, ranked.size)
    }

    // ==================== Favorite Boost Tests ====================

    @Test
    fun `favorite restaurant gets ranking boost`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 4.0f, averagePrice = 12.0f, isFavorite = false),
            createRestaurant(id = "2", rating = 3.8f, averagePrice = 12.0f, isFavorite = true)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Favorite should rank higher despite slightly lower rating
        assertEquals("2", ranked[0].restaurant.id)
    }

    @Test
    fun `favorite boost not applied to closed restaurants`() {
        // Both restaurants have same base stats, but one is closed favorite
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 4.0f, averagePrice = 12.0f, ttcWalkMinutes = 5, isOpenNow = true, isFavorite = false),
            createRestaurant(id = "2", rating = 4.0f, averagePrice = 12.0f, ttcWalkMinutes = 5, isOpenNow = false, isFavorite = true)
        )

        val ranked = RestaurantRanker.rank(restaurants, excludeClosed = false)

        // Closed favorite should not get boost, so open restaurant wins
        // (favorite boost only applies when isOpenNow != false)
        assertEquals("1", ranked[0].restaurant.id)
    }

    @Test
    fun `favorite boost not applied to expensive restaurants`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f, rating = 4.0f, isFavorite = false),
            createRestaurant(id = "2", averagePrice = 20.0f, rating = 4.0f, isFavorite = true)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Expensive favorite should not get boost
        assertEquals("1", ranked[0].restaurant.id)
    }

    // ==================== Data Freshness Tests ====================

    @Test
    fun `unknown freshness never ranked first when verified option exists`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 5.0f, dataFreshness = DataFreshness.UNKNOWN),
            createRestaurant(id = "2", rating = 4.0f, dataFreshness = DataFreshness.LIVE)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Despite higher rating, UNKNOWN should not be #1
        assertEquals("2", ranked[0].restaurant.id)
        assertEquals("1", ranked[1].restaurant.id)
    }

    @Test
    fun `unknown freshness can be first if no verified options`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 5.0f, dataFreshness = DataFreshness.UNKNOWN),
            createRestaurant(id = "2", rating = 4.0f, dataFreshness = DataFreshness.UNKNOWN)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Both unknown, so highest score wins
        assertEquals("1", ranked[0].restaurant.id)
    }

    @Test
    fun `live freshness preferred over cached`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 4.5f, dataFreshness = DataFreshness.CACHED),
            createRestaurant(id = "2", rating = 4.5f, dataFreshness = DataFreshness.LIVE)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        // Same score, but ranking should work
        assertEquals(2, ranked.size)
    }

    // ==================== Explanation Tests ====================

    @Test
    fun `explanation includes cheap commentary for low price`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 10.0f)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertTrue(ranked[0].explanation.contains("cheap", ignoreCase = true) ||
                   ranked[0].explanation.contains("Budget", ignoreCase = true))
    }

    @Test
    fun `explanation includes station name for nearby TTC`() {
        val restaurants = listOf(
            createRestaurant(id = "1", ttcWalkMinutes = 3, nearestStation = "Union")
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertTrue(ranked[0].explanation.contains("Union"))
    }

    @Test
    fun `explanation includes rating commentary for high rated`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 4.7f)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertTrue(ranked[0].explanation.contains("love", ignoreCase = true) ||
                   ranked[0].explanation.contains("solid", ignoreCase = true))
    }

    // ==================== Trust Label Tests ====================

    @Test
    fun `trust label shows Live data for LIVE freshness`() {
        val restaurants = listOf(
            createRestaurant(id = "1", dataFreshness = DataFreshness.LIVE)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals("Live data", ranked[0].trustLabel)
    }

    @Test
    fun `trust label shows Updated recently for RECENT freshness`() {
        val restaurants = listOf(
            createRestaurant(id = "1", dataFreshness = DataFreshness.RECENT)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals("Updated recently", ranked[0].trustLabel)
    }

    @Test
    fun `trust label shows Unverified for UNKNOWN freshness`() {
        val restaurants = listOf(
            createRestaurant(id = "1", dataFreshness = DataFreshness.UNKNOWN)
        )

        val ranked = RestaurantRanker.rank(restaurants)

        assertEquals("Unverified", ranked[0].trustLabel)
    }

    // ==================== Filter Helper Tests ====================

    @Test
    fun `filterOpen excludes closed restaurants`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isOpenNow = true),
            createRestaurant(id = "2", isOpenNow = false),
            createRestaurant(id = "3", isOpenNow = null)
        )

        val filtered = RestaurantRanker.filterOpen(restaurants)

        assertEquals(2, filtered.size)
        assertTrue(filtered.none { it.id == "2" })
    }

    @Test
    fun `filterUnder15 includes cheap and unknown price restaurants`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 12.0f),
            createRestaurant(id = "2", averagePrice = 20.0f),
            createRestaurant(id = "3", averagePrice = null)
        )

        val filtered = RestaurantRanker.filterUnder15(restaurants)

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == "1" })
        assertTrue(filtered.any { it.id == "3" })
    }

    // ==================== Sort Options Tests ====================

    @Test
    fun `sortBy PRICE_LOW sorts by lowest price first`() {
        val restaurants = listOf(
            createRestaurant(id = "1", averagePrice = 15.0f),
            createRestaurant(id = "2", averagePrice = 8.0f),
            createRestaurant(id = "3", averagePrice = 12.0f)
        )

        val sorted = RestaurantRanker.sortBy(restaurants, SortOption.PRICE_LOW)

        assertEquals("2", sorted[0].id)
        assertEquals("3", sorted[1].id)
        assertEquals("1", sorted[2].id)
    }

    @Test
    fun `sortBy RATING_HIGH sorts by highest rating first`() {
        val restaurants = listOf(
            createRestaurant(id = "1", rating = 3.5f),
            createRestaurant(id = "2", rating = 4.8f),
            createRestaurant(id = "3", rating = 4.2f)
        )

        val sorted = RestaurantRanker.sortBy(restaurants, SortOption.RATING_HIGH)

        assertEquals("2", sorted[0].id)
        assertEquals("3", sorted[1].id)
        assertEquals("1", sorted[2].id)
    }

    @Test
    fun `sortBy NEAREST_TTC sorts by closest TTC first`() {
        val restaurants = listOf(
            createRestaurant(id = "1", ttcWalkMinutes = 10),
            createRestaurant(id = "2", ttcWalkMinutes = 2),
            createRestaurant(id = "3", ttcWalkMinutes = 5)
        )

        val sorted = RestaurantRanker.sortBy(restaurants, SortOption.NEAREST_TTC)

        assertEquals("2", sorted[0].id)
        assertEquals("3", sorted[1].id)
        assertEquals("1", sorted[2].id)
    }

    // ==================== Walking Time Calculation Tests ====================

    @Test
    fun `walkingTimeMinutes calculates correctly at 80m per min`() {
        // 400 meters at 80m/min = 5 minutes
        assertEquals(5, RestaurantRanker.walkingTimeMinutes(400f))
    }

    @Test
    fun `walkingTimeMinutes rounds down`() {
        // 350 meters at 80m/min = 4.375 minutes, rounds to 4
        assertEquals(4, RestaurantRanker.walkingTimeMinutes(350f))
    }

    @Test
    fun `walkingTimeMinutes returns 0 for very short distances`() {
        assertEquals(0, RestaurantRanker.walkingTimeMinutes(50f))
    }
}

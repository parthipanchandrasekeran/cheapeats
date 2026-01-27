package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Restaurant data class.
 */
class RestaurantTest {

    private fun createRestaurant(
        id: String = "1",
        name: String = "Test Restaurant",
        cuisine: String = "Italian",
        priceLevel: Int = 2,
        rating: Float = 4.5f,
        distance: Float = 0.5f,
        imageUrl: String? = null,
        address: String = "123 Test St",
        location: LatLng = LatLng(43.7615, -79.3456),
        isSponsored: Boolean = false,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false,
        averagePrice: Float? = null,
        websiteUrl: String? = null,
        googleMapsUrl: String? = null
    ) = Restaurant(
        id, name, cuisine, priceLevel, rating, distance, imageUrl, address,
        location, isSponsored, hasStudentDiscount, nearTTC, averagePrice, websiteUrl, googleMapsUrl
    )

    // ==================== pricePoint Tests ====================

    @Test
    fun `pricePoint returns Free for priceLevel 0`() {
        val restaurant = createRestaurant(priceLevel = 0)
        assertEquals("Free", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint returns single dollar sign for priceLevel 1`() {
        val restaurant = createRestaurant(priceLevel = 1)
        assertEquals("$", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint returns double dollar signs for priceLevel 2`() {
        val restaurant = createRestaurant(priceLevel = 2)
        assertEquals("$$", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint returns triple dollar signs for priceLevel 3`() {
        val restaurant = createRestaurant(priceLevel = 3)
        assertEquals("$$$", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint returns four dollar signs for priceLevel 4`() {
        val restaurant = createRestaurant(priceLevel = 4)
        assertEquals("$$$$", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint defaults to single dollar sign for invalid priceLevel`() {
        val restaurant = createRestaurant(priceLevel = 5)
        assertEquals("$", restaurant.pricePoint)
    }

    @Test
    fun `pricePoint defaults to single dollar sign for negative priceLevel`() {
        val restaurant = createRestaurant(priceLevel = -1)
        assertEquals("$", restaurant.pricePoint)
    }

    // ==================== isUnder15 Tests ====================

    @Test
    fun `isUnder15 returns true when averagePrice is below 15`() {
        val restaurant = createRestaurant(averagePrice = 14.99f)
        assertTrue(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 returns false when averagePrice is 15 or above`() {
        val restaurant = createRestaurant(averagePrice = 15.00f)
        assertFalse(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 returns false when averagePrice is above 15`() {
        val restaurant = createRestaurant(averagePrice = 20.00f)
        assertFalse(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 uses priceLevel when averagePrice is null and priceLevel is 0`() {
        val restaurant = createRestaurant(averagePrice = null, priceLevel = 0)
        assertTrue(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 uses priceLevel when averagePrice is null and priceLevel is 1`() {
        val restaurant = createRestaurant(averagePrice = null, priceLevel = 1)
        assertTrue(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 uses priceLevel when averagePrice is null and priceLevel is 2`() {
        val restaurant = createRestaurant(averagePrice = null, priceLevel = 2)
        assertFalse(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 prioritizes averagePrice over priceLevel`() {
        // Low priceLevel but high averagePrice - should use averagePrice
        val restaurant = createRestaurant(averagePrice = 25.00f, priceLevel = 1)
        assertFalse(restaurant.isUnder15)
    }

    @Test
    fun `isUnder15 returns true for very cheap average price`() {
        val restaurant = createRestaurant(averagePrice = 5.00f)
        assertTrue(restaurant.isUnder15)
    }

    // ==================== Convenience Properties Tests ====================

    @Test
    fun `latitude returns correct value from location`() {
        val location = LatLng(43.7615, -79.3456)
        val restaurant = createRestaurant(location = location)
        assertEquals(43.7615, restaurant.latitude, 0.0001)
    }

    @Test
    fun `longitude returns correct value from location`() {
        val location = LatLng(43.7615, -79.3456)
        val restaurant = createRestaurant(location = location)
        assertEquals(-79.3456, restaurant.longitude, 0.0001)
    }

    // ==================== Data Class Equality Tests ====================

    @Test
    fun `restaurants with same properties are equal`() {
        val restaurant1 = createRestaurant(id = "1", name = "Test")
        val restaurant2 = createRestaurant(id = "1", name = "Test")
        assertEquals(restaurant1, restaurant2)
    }

    @Test
    fun `restaurants with different ids are not equal`() {
        val restaurant1 = createRestaurant(id = "1")
        val restaurant2 = createRestaurant(id = "2")
        assertNotEquals(restaurant1, restaurant2)
    }

    @Test
    fun `restaurant copy works correctly`() {
        val original = createRestaurant(name = "Original")
        val copy = original.copy(name = "Copy")
        assertEquals("Copy", copy.name)
        assertEquals(original.id, copy.id)
    }

    // ==================== Default Values Tests ====================

    @Test
    fun `restaurant has correct default values`() {
        val restaurant = createRestaurant()
        assertFalse(restaurant.isSponsored)
        assertFalse(restaurant.hasStudentDiscount)
        assertFalse(restaurant.nearTTC)
        assertNull(restaurant.averagePrice)
        assertNull(restaurant.websiteUrl)
        assertNull(restaurant.googleMapsUrl)
    }
}

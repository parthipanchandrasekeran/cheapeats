package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RestaurantEntity data class.
 */
class RestaurantEntityTest {

    private fun createEntity(
        id: Long = 0,
        placeId: String = "place_1",
        name: String = "Test Restaurant",
        cuisine: String = "Italian",
        priceLevel: Int = 2,
        rating: Float = 4.5f,
        address: String = "123 Test St",
        latitude: Double = 43.7615,
        longitude: Double = -79.3456,
        isSponsored: Boolean = false,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false,
        averagePrice: Float? = null,
        imageUrl: String? = null,
        phoneNumber: String? = null,
        website: String? = null,
        isFavorite: Boolean = false
    ) = RestaurantEntity(
        id = id,
        placeId = placeId,
        name = name,
        cuisine = cuisine,
        priceLevel = priceLevel,
        rating = rating,
        address = address,
        latitude = latitude,
        longitude = longitude,
        isSponsored = isSponsored,
        hasStudentDiscount = hasStudentDiscount,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        imageUrl = imageUrl,
        phoneNumber = phoneNumber,
        website = website,
        isFavorite = isFavorite
    )

    // ==================== Default Values Tests ====================

    @Test
    fun `default id is 0`() {
        val entity = createEntity()
        assertEquals(0L, entity.id)
    }

    @Test
    fun `default isSponsored is false`() {
        val entity = createEntity()
        assertFalse(entity.isSponsored)
    }

    @Test
    fun `default hasStudentDiscount is false`() {
        val entity = createEntity()
        assertFalse(entity.hasStudentDiscount)
    }

    @Test
    fun `default nearTTC is false`() {
        val entity = createEntity()
        assertFalse(entity.nearTTC)
    }

    @Test
    fun `default averagePrice is null`() {
        val entity = createEntity()
        assertNull(entity.averagePrice)
    }

    @Test
    fun `default imageUrl is null`() {
        val entity = createEntity()
        assertNull(entity.imageUrl)
    }

    @Test
    fun `default phoneNumber is null`() {
        val entity = createEntity()
        assertNull(entity.phoneNumber)
    }

    @Test
    fun `default website is null`() {
        val entity = createEntity()
        assertNull(entity.website)
    }

    @Test
    fun `default isFavorite is false`() {
        val entity = createEntity()
        assertFalse(entity.isFavorite)
    }

    @Test
    fun `lastUpdated is set automatically`() {
        val beforeCreation = System.currentTimeMillis()
        val entity = createEntity()
        val afterCreation = System.currentTimeMillis()

        assertTrue(entity.lastUpdated >= beforeCreation)
        assertTrue(entity.lastUpdated <= afterCreation)
    }

    // ==================== Field Storage Tests ====================

    @Test
    fun `stores all required fields correctly`() {
        val entity = createEntity(
            placeId = "place_123",
            name = "Great Restaurant",
            cuisine = "Japanese",
            priceLevel = 3,
            rating = 4.8f,
            address = "456 Main St",
            latitude = 43.6532,
            longitude = -79.3832
        )

        assertEquals("place_123", entity.placeId)
        assertEquals("Great Restaurant", entity.name)
        assertEquals("Japanese", entity.cuisine)
        assertEquals(3, entity.priceLevel)
        assertEquals(4.8f, entity.rating, 0.001f)
        assertEquals("456 Main St", entity.address)
        assertEquals(43.6532, entity.latitude, 0.0001)
        assertEquals(-79.3832, entity.longitude, 0.0001)
    }

    @Test
    fun `stores all optional fields correctly`() {
        val entity = createEntity(
            isSponsored = true,
            hasStudentDiscount = true,
            nearTTC = true,
            averagePrice = 25.00f,
            imageUrl = "https://example.com/image.jpg",
            phoneNumber = "416-555-1234",
            website = "https://restaurant.com",
            isFavorite = true
        )

        assertTrue(entity.isSponsored)
        assertTrue(entity.hasStudentDiscount)
        assertTrue(entity.nearTTC)
        assertEquals(25.00f, entity.averagePrice!!, 0.001f)
        assertEquals("https://example.com/image.jpg", entity.imageUrl)
        assertEquals("416-555-1234", entity.phoneNumber)
        assertEquals("https://restaurant.com", entity.website)
        assertTrue(entity.isFavorite)
    }

    // ==================== Price Level Validation Tests ====================

    @Test
    fun `priceLevel can be 0 (Free)`() {
        val entity = createEntity(priceLevel = 0)
        assertEquals(0, entity.priceLevel)
    }

    @Test
    fun `priceLevel can be 1 (Inexpensive)`() {
        val entity = createEntity(priceLevel = 1)
        assertEquals(1, entity.priceLevel)
    }

    @Test
    fun `priceLevel can be 2 (Moderate)`() {
        val entity = createEntity(priceLevel = 2)
        assertEquals(2, entity.priceLevel)
    }

    @Test
    fun `priceLevel can be 3 (Expensive)`() {
        val entity = createEntity(priceLevel = 3)
        assertEquals(3, entity.priceLevel)
    }

    @Test
    fun `priceLevel can be 4 (Very Expensive)`() {
        val entity = createEntity(priceLevel = 4)
        assertEquals(4, entity.priceLevel)
    }

    // ==================== Rating Validation Tests ====================

    @Test
    fun `rating can be 0`() {
        val entity = createEntity(rating = 0f)
        assertEquals(0f, entity.rating, 0.001f)
    }

    @Test
    fun `rating can be 5`() {
        val entity = createEntity(rating = 5f)
        assertEquals(5f, entity.rating, 0.001f)
    }

    @Test
    fun `rating can be decimal`() {
        val entity = createEntity(rating = 4.7f)
        assertEquals(4.7f, entity.rating, 0.001f)
    }

    // ==================== Coordinate Validation Tests ====================

    @Test
    fun `coordinates can store Toronto downtown`() {
        val entity = createEntity(latitude = 43.6532, longitude = -79.3832)
        assertEquals(43.6532, entity.latitude, 0.0001)
        assertEquals(-79.3832, entity.longitude, 0.0001)
    }

    @Test
    fun `coordinates can store North York`() {
        val entity = createEntity(latitude = 43.7615, longitude = -79.4111)
        assertEquals(43.7615, entity.latitude, 0.0001)
        assertEquals(-79.4111, entity.longitude, 0.0001)
    }

    // ==================== Data Class Tests ====================

    @Test
    fun `entities with same data are equal`() {
        val entity1 = RestaurantEntity(
            id = 1,
            placeId = "place_1",
            name = "Test",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            address = "123 Test",
            latitude = 43.0,
            longitude = -79.0,
            lastUpdated = 1000L
        )
        val entity2 = RestaurantEntity(
            id = 1,
            placeId = "place_1",
            name = "Test",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            address = "123 Test",
            latitude = 43.0,
            longitude = -79.0,
            lastUpdated = 1000L
        )
        assertEquals(entity1, entity2)
    }

    @Test
    fun `entities with different ids are not equal`() {
        val entity1 = createEntity(id = 1)
        val entity2 = createEntity(id = 2)
        assertNotEquals(entity1, entity2)
    }

    @Test
    fun `copy works correctly`() {
        val original = createEntity(name = "Original", isFavorite = false)
        val copy = original.copy(name = "Copy", isFavorite = true)

        assertEquals("Copy", copy.name)
        assertTrue(copy.isFavorite)
        assertEquals(original.placeId, copy.placeId)
        assertEquals(original.cuisine, copy.cuisine)
    }

    @Test
    fun `hashCode is consistent`() {
        val entity1 = RestaurantEntity(
            id = 1,
            placeId = "place_1",
            name = "Test",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            address = "123 Test",
            latitude = 43.0,
            longitude = -79.0,
            lastUpdated = 1000L
        )
        val entity2 = RestaurantEntity(
            id = 1,
            placeId = "place_1",
            name = "Test",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            address = "123 Test",
            latitude = 43.0,
            longitude = -79.0,
            lastUpdated = 1000L
        )
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }

    // ==================== Edge Cases ====================

    @Test
    fun `handles empty strings`() {
        val entity = createEntity(
            placeId = "",
            name = "",
            cuisine = "",
            address = ""
        )
        assertEquals("", entity.placeId)
        assertEquals("", entity.name)
        assertEquals("", entity.cuisine)
        assertEquals("", entity.address)
    }

    @Test
    fun `handles special characters in name`() {
        val entity = createEntity(name = "Café Résumé & Grill 日本語")
        assertEquals("Café Résumé & Grill 日本語", entity.name)
    }

    @Test
    fun `handles long addresses`() {
        val longAddress = "123 Very Long Street Name, Unit 456, Toronto, Ontario, Canada, M5V 1A1"
        val entity = createEntity(address = longAddress)
        assertEquals(longAddress, entity.address)
    }
}

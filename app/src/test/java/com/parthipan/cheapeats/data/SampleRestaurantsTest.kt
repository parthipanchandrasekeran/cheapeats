package com.parthipan.cheapeats.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the sampleRestaurants list.
 */
class SampleRestaurantsTest {

    // ==================== List Structure Tests ====================

    @Test
    fun `sampleRestaurants is not empty`() {
        assertTrue(sampleRestaurants.isNotEmpty())
    }

    @Test
    fun `sampleRestaurants has 12 restaurants`() {
        assertEquals(12, sampleRestaurants.size)
    }

    @Test
    fun `all restaurants have unique ids`() {
        val ids = sampleRestaurants.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all restaurants have names`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(restaurant.name.isNotBlank())
        }
    }

    // ==================== Restaurant Content Tests ====================

    @Test
    fun `sampleRestaurants contains Banh Mi Nguyen Huong`() {
        val restaurant = sampleRestaurants.find { it.name == "Banh Mi Nguyen Huong" }
        assertNotNull(restaurant)
        assertEquals("Vietnamese", restaurant!!.cuisine)
        assertEquals(1, restaurant.priceLevel)
    }

    @Test
    fun `sampleRestaurants contains Juicy Dumpling`() {
        val restaurant = sampleRestaurants.find { it.name == "Juicy Dumpling" }
        assertNotNull(restaurant)
        assertEquals("Chinese", restaurant!!.cuisine)
        assertEquals(1, restaurant.priceLevel)
    }

    @Test
    fun `sampleRestaurants contains Mom's Pan Fried Bun`() {
        val restaurant = sampleRestaurants.find { it.name == "Mom's Pan Fried Bun" }
        assertNotNull(restaurant)
        assertEquals("Chinese", restaurant!!.cuisine)
        assertTrue(restaurant.hasStudentDiscount)
    }

    @Test
    fun `sampleRestaurants contains Salad King`() {
        val restaurant = sampleRestaurants.find { it.name == "Salad King" }
        assertNotNull(restaurant)
        assertEquals("Thai", restaurant!!.cuisine)
        assertTrue(restaurant.hasStudentDiscount)
    }

    @Test
    fun `sampleRestaurants contains Jin Dal Lae`() {
        val restaurant = sampleRestaurants.find { it.name == "Jin Dal Lae" }
        assertNotNull(restaurant)
        assertEquals("Korean", restaurant!!.cuisine)
        assertTrue(restaurant.nearTTC)
    }

    @Test
    fun `sampleRestaurants contains Ghazale`() {
        val restaurant = sampleRestaurants.find { it.name == "Ghazale" }
        assertNotNull(restaurant)
        assertEquals("Lebanese/Middle Eastern", restaurant!!.cuisine)
        assertTrue(restaurant.hasStudentDiscount)
    }

    @Test
    fun `sampleRestaurants contains Pho Hung`() {
        val restaurant = sampleRestaurants.find { it.name == "Pho Hung" }
        assertNotNull(restaurant)
        assertEquals("Vietnamese", restaurant!!.cuisine)
        assertTrue(restaurant.nearTTC)
    }

    @Test
    fun `sampleRestaurants contains Udupi Palace`() {
        val restaurant = sampleRestaurants.find { it.name == "Udupi Palace" }
        assertNotNull(restaurant)
        assertEquals("South Indian Vegetarian", restaurant!!.cuisine)
        assertTrue(restaurant.hasStudentDiscount)
    }

    // ==================== Location Tests ====================

    @Test
    fun `all restaurants are in Toronto area`() {
        sampleRestaurants.forEach { restaurant ->
            // Toronto is approximately at latitude 43.6-43.9, longitude -79.2 to -79.6
            assertTrue(
                "Restaurant ${restaurant.name} has invalid latitude",
                restaurant.latitude in 43.5..44.0
            )
            assertTrue(
                "Restaurant ${restaurant.name} has invalid longitude",
                restaurant.longitude in -80.0..-79.0
            )
        }
    }

    @Test
    fun `restaurants are centered around downtown Toronto`() {
        // Downtown Toronto is approximately at 43.66, -79.39
        val avgLatitude = sampleRestaurants.map { it.latitude }.average()
        val avgLongitude = sampleRestaurants.map { it.longitude }.average()

        assertEquals(43.67, avgLatitude, 0.05)
        assertEquals(-79.38, avgLongitude, 0.05)
    }

    // ==================== Price Level Tests ====================

    @Test
    fun `restaurants have valid price levels`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} has invalid price level ${restaurant.priceLevel}",
                restaurant.priceLevel in 0..4
            )
        }
    }

    @Test
    fun `most restaurants have price level 1`() {
        val cheapRestaurants = sampleRestaurants.filter { it.priceLevel == 1 }
        assertTrue(cheapRestaurants.size >= 8)
    }

    // ==================== Rating Tests ====================

    @Test
    fun `all restaurants have valid ratings`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} has invalid rating ${restaurant.rating}",
                restaurant.rating in 0f..5f
            )
        }
    }

    @Test
    fun `all restaurants have positive ratings`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(restaurant.rating > 0f)
        }
    }

    // ==================== Distance Tests ====================

    @Test
    fun `all restaurants have positive distances`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} has invalid distance ${restaurant.distance}",
                restaurant.distance >= 0f
            )
        }
    }

    @Test
    fun `all restaurants are within reasonable distance`() {
        // Sample restaurants should be within 2 miles
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} is too far: ${restaurant.distance}",
                restaurant.distance <= 2f
            )
        }
    }

    // ==================== Filter Feature Tests ====================

    @Test
    fun `some restaurants have student discount`() {
        val withDiscount = sampleRestaurants.filter { it.hasStudentDiscount }
        assertTrue(withDiscount.isNotEmpty())
        assertTrue(withDiscount.size >= 3)
    }

    @Test
    fun `some restaurants are near TTC`() {
        val nearTTC = sampleRestaurants.filter { it.nearTTC }
        assertTrue(nearTTC.isNotEmpty())
        assertTrue(nearTTC.size >= 3)
    }

    @Test
    fun `some restaurants are under 15 dollars`() {
        val under15 = sampleRestaurants.filter { it.isUnder15 }
        assertTrue(under15.isNotEmpty())
    }

    // ==================== Average Price Tests ====================

    @Test
    fun `all restaurants have average price set`() {
        sampleRestaurants.forEach { restaurant ->
            assertNotNull(
                "Restaurant ${restaurant.name} should have average price",
                restaurant.averagePrice
            )
        }
    }

    @Test
    fun `average prices are realistic`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} has unrealistic price ${restaurant.averagePrice}",
                restaurant.averagePrice!! in 1f..50f
            )
        }
    }

    // ==================== Cuisine Variety Tests ====================

    @Test
    fun `sample restaurants have diverse cuisines`() {
        val cuisines = sampleRestaurants.map { it.cuisine }.toSet()
        assertTrue(
            "Expected at least 5 different cuisines, got ${cuisines.size}",
            cuisines.size >= 5
        )
    }

    @Test
    fun `cuisines include common types`() {
        val cuisines = sampleRestaurants.map { it.cuisine }.toSet()
        assertTrue(cuisines.contains("Vietnamese"))
        assertTrue(cuisines.contains("Chinese"))
        assertTrue(cuisines.contains("Thai"))
        assertTrue(cuisines.contains("Korean"))
    }

    // ==================== Address Tests ====================

    @Test
    fun `all restaurants have addresses`() {
        sampleRestaurants.forEach { restaurant ->
            assertTrue(
                "Restaurant ${restaurant.name} should have address",
                restaurant.address.isNotBlank()
            )
        }
    }

    @Test
    fun `addresses contain Toronto`() {
        val torontoAddresses = sampleRestaurants.count {
            it.address.contains("Toronto", ignoreCase = true) ||
                it.address.contains("Scarborough", ignoreCase = true)
        }
        assertTrue(
            "Expected most addresses to contain Toronto or Scarborough",
            torontoAddresses >= 8
        )
    }
}

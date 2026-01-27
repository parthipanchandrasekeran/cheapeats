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
    fun `sampleRestaurants has 8 restaurants`() {
        assertEquals(8, sampleRestaurants.size)
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
    fun `sampleRestaurants contains Taco Paradise`() {
        val tacoParadise = sampleRestaurants.find { it.name == "Taco Paradise" }
        assertNotNull(tacoParadise)
        assertEquals("Mexican", tacoParadise!!.cuisine)
        assertEquals(1, tacoParadise.priceLevel)
        assertTrue(tacoParadise.isSponsored)
    }

    @Test
    fun `sampleRestaurants contains Pasta House`() {
        val pastaHouse = sampleRestaurants.find { it.name == "Pasta House" }
        assertNotNull(pastaHouse)
        assertEquals("Italian", pastaHouse!!.cuisine)
        assertEquals(2, pastaHouse.priceLevel)
        assertFalse(pastaHouse.isSponsored)
    }

    @Test
    fun `sampleRestaurants contains Golden Dragon`() {
        val goldenDragon = sampleRestaurants.find { it.name == "Golden Dragon" }
        assertNotNull(goldenDragon)
        assertEquals("Chinese", goldenDragon!!.cuisine)
        assertTrue(goldenDragon.isSponsored)
        assertTrue(goldenDragon.hasStudentDiscount)
    }

    @Test
    fun `sampleRestaurants contains Burger Barn`() {
        val burgerBarn = sampleRestaurants.find { it.name == "Burger Barn" }
        assertNotNull(burgerBarn)
        assertEquals("American", burgerBarn!!.cuisine)
        assertTrue(burgerBarn.hasStudentDiscount)
        assertTrue(burgerBarn.nearTTC)
    }

    @Test
    fun `sampleRestaurants contains Sushi Zen`() {
        val sushiZen = sampleRestaurants.find { it.name == "Sushi Zen" }
        assertNotNull(sushiZen)
        assertEquals("Japanese", sushiZen!!.cuisine)
        assertEquals(2, sushiZen.priceLevel)
    }

    @Test
    fun `sampleRestaurants contains Curry Corner`() {
        val curryCorner = sampleRestaurants.find { it.name == "Curry Corner" }
        assertNotNull(curryCorner)
        assertEquals("Indian", curryCorner!!.cuisine)
        assertTrue(curryCorner.hasStudentDiscount)
    }

    @Test
    fun `sampleRestaurants contains Mediterranean Grill`() {
        val medGrill = sampleRestaurants.find { it.name == "Mediterranean Grill" }
        assertNotNull(medGrill)
        assertEquals("Mediterranean", medGrill!!.cuisine)
        assertTrue(medGrill.isSponsored)
    }

    @Test
    fun `sampleRestaurants contains Pho Express`() {
        val phoExpress = sampleRestaurants.find { it.name == "Pho Express" }
        assertNotNull(phoExpress)
        assertEquals("Vietnamese", phoExpress!!.cuisine)
        assertTrue(phoExpress.hasStudentDiscount)
        assertTrue(phoExpress.nearTTC)
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
    fun `restaurants are centered around York Mills area`() {
        // York Mills is approximately at 43.7615, -79.3456
        val avgLatitude = sampleRestaurants.map { it.latitude }.average()
        val avgLongitude = sampleRestaurants.map { it.longitude }.average()

        assertEquals(43.76, avgLatitude, 0.02)
        assertEquals(-79.35, avgLongitude, 0.02)
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
    fun `some restaurants have price level 1`() {
        val cheapRestaurants = sampleRestaurants.filter { it.priceLevel == 1 }
        assertTrue(cheapRestaurants.isNotEmpty())
    }

    @Test
    fun `some restaurants have price level 2`() {
        val moderateRestaurants = sampleRestaurants.filter { it.priceLevel == 2 }
        assertTrue(moderateRestaurants.isNotEmpty())
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
        assertTrue(withDiscount.size >= 3) // At least a few should have discounts
    }

    @Test
    fun `some restaurants are near TTC`() {
        val nearTTC = sampleRestaurants.filter { it.nearTTC }
        assertTrue(nearTTC.isNotEmpty())
        assertTrue(nearTTC.size >= 3)
    }

    @Test
    fun `some restaurants are sponsored`() {
        val sponsored = sampleRestaurants.filter { it.isSponsored }
        assertTrue(sponsored.isNotEmpty())
        assertTrue(sponsored.size >= 2)
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
                restaurant.averagePrice!! in 5f..50f
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
        assertTrue(cuisines.contains("Mexican"))
        assertTrue(cuisines.contains("Italian"))
        assertTrue(cuisines.contains("Chinese"))
        assertTrue(cuisines.contains("Japanese"))
        assertTrue(cuisines.contains("Indian"))
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
    fun `addresses contain York Mills`() {
        val yorkMillsAddresses = sampleRestaurants.count {
            it.address.contains("York Mills", ignoreCase = true)
        }
        assertTrue(
            "Expected most addresses to contain York Mills",
            yorkMillsAddresses >= 5
        )
    }
}

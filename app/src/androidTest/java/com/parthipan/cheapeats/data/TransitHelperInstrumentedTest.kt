package com.parthipan.cheapeats.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TransitHelper distance calculations.
 * These tests require Android's Location.distanceBetween() API.
 */
@RunWith(AndroidJUnit4::class)
class TransitHelperInstrumentedTest {

    // ==================== calculateDistanceMeters Tests ====================

    @Test
    fun calculateDistanceMeters_samePoint_returnsZero() {
        val point = LatLng(43.6453, -79.3806)
        val distance = TransitHelper.calculateDistanceMeters(point, point)
        assertEquals(0f, distance, 1f) // Allow 1 meter tolerance
    }

    @Test
    fun calculateDistanceMeters_knownDistance_returnsApproximately() {
        // Union to Bloor-Yonge is approximately 2.8 km
        val union = LatLng(43.6453, -79.3806)
        val bloorYonge = LatLng(43.6709, -79.3857)
        val distance = TransitHelper.calculateDistanceMeters(union, bloorYonge)

        // Should be around 2800 meters (2.8 km)
        assertTrue("Distance should be around 2800m, was $distance", distance in 2500f..3100f)
    }

    @Test
    fun calculateDistanceMeters_shortDistance_accurate() {
        // Two points ~100m apart
        val point1 = LatLng(43.6453, -79.3806)
        val point2 = LatLng(43.6462, -79.3806) // ~100m north
        val distance = TransitHelper.calculateDistanceMeters(point1, point2)

        assertTrue("Short distance should be around 100m, was $distance", distance in 50f..150f)
    }

    // ==================== isTransitAccessible Tests ====================

    @Test
    fun isTransitAccessible_atStation_returnsTrue() {
        // Exactly at Union Station
        val unionLocation = LatLng(43.6453, -79.3806)
        val result = TransitHelper.isTransitAccessible(unionLocation)
        assertTrue("Should be accessible at Union Station", result)
    }

    @Test
    fun isTransitAccessible_nearStation_returnsTrue() {
        // 200m from Union Station
        val nearUnion = LatLng(43.6470, -79.3806)
        val result = TransitHelper.isTransitAccessible(nearUnion)
        assertTrue("Should be accessible near Union Station", result)
    }

    @Test
    fun isTransitAccessible_farFromStation_returnsFalse() {
        // Middle of Lake Ontario - far from any station
        val lakeOntario = LatLng(43.5, -79.3)
        val result = TransitHelper.isTransitAccessible(lakeOntario)
        assertFalse("Should not be accessible in Lake Ontario", result)
    }

    @Test
    fun isTransitAccessible_customRadius_works() {
        // 600m from Union Station
        val slightlyFar = LatLng(43.6510, -79.3806)

        // With default 500m radius
        val defaultResult = TransitHelper.isTransitAccessible(slightlyFar, 500.0)
        // With larger 1000m radius
        val largerResult = TransitHelper.isTransitAccessible(slightlyFar, 1000.0)

        assertTrue("Should be accessible with larger radius", largerResult)
    }

    @Test
    fun isTransitAccessible_customStations_works() {
        val testLocation = LatLng(43.6453, -79.3806) // Union
        val customStations = listOf(
            SubwayStation("Test Station", LatLng(43.6453, -79.3806), listOf("Test Line"))
        )

        val result = TransitHelper.isTransitAccessible(testLocation, 500.0, customStations)
        assertTrue("Should be accessible with custom station list", result)
    }

    // ==================== findNearestStation Tests ====================

    @Test
    fun findNearestStation_atStation_findsIt() {
        val unionLocation = LatLng(43.6453, -79.3806)
        val result = TransitHelper.findNearestStation(unionLocation)

        assertNotNull(result)
        assertEquals("Union", result!!.first.name)
        assertTrue("Distance should be very small", result.second < 50f)
    }

    @Test
    fun findNearestStation_nearBloorYonge_findsIt() {
        val nearBloorYonge = LatLng(43.6710, -79.3858)
        val result = TransitHelper.findNearestStation(nearBloorYonge)

        assertNotNull(result)
        assertEquals("Bloor-Yonge", result!!.first.name)
    }

    @Test
    fun findNearestStation_emptyList_returnsNull() {
        val location = LatLng(43.6453, -79.3806)
        val result = TransitHelper.findNearestStation(location, emptyList())
        assertNull(result)
    }

    @Test
    fun findNearestStation_returnsDistanceInMeters() {
        val testLocation = LatLng(43.6463, -79.3806) // ~100m from Union
        val result = TransitHelper.findNearestStation(testLocation)

        assertNotNull(result)
        assertTrue("Distance should be in meters", result!!.second > 0)
        assertTrue("Distance should be reasonable", result.second < 10000f)
    }

    // ==================== getStationsWithinRadius Tests ====================

    @Test
    fun getStationsWithinRadius_atDowntownCore_findsMultiple() {
        // Downtown Toronto should have multiple stations within 1km
        val downtown = LatLng(43.6532, -79.3832)
        val result = TransitHelper.getStationsWithinRadius(downtown, 1000.0)

        assertTrue("Should find multiple stations downtown", result.size >= 2)
    }

    @Test
    fun getStationsWithinRadius_resultsSortedByDistance() {
        val location = LatLng(43.6532, -79.3832)
        val result = TransitHelper.getStationsWithinRadius(location, 2000.0)

        if (result.size >= 2) {
            for (i in 0 until result.size - 1) {
                assertTrue(
                    "Results should be sorted by distance",
                    result[i].second <= result[i + 1].second
                )
            }
        }
    }

    @Test
    fun getStationsWithinRadius_verySmallRadius_mayBeEmpty() {
        // Very small radius in lake
        val lakeOntario = LatLng(43.5, -79.3)
        val result = TransitHelper.getStationsWithinRadius(lakeOntario, 100.0)

        assertTrue("Should find no stations in lake", result.isEmpty())
    }

    @Test
    fun getStationsWithinRadius_atStation_includesIt() {
        val unionLocation = LatLng(43.6453, -79.3806)
        val result = TransitHelper.getStationsWithinRadius(unionLocation, 100.0)

        assertTrue("Should include Union Station", result.any { it.first.name == "Union" })
    }

    // ==================== isNearMajorHub Tests ====================

    @Test
    fun isNearMajorHub_atUnion_returnsTrue() {
        val unionLocation = LatLng(43.6453, -79.3806)
        val result = TransitHelper.isNearMajorHub(unionLocation)
        assertTrue("Union is a major hub", result)
    }

    @Test
    fun isNearMajorHub_atBloorYonge_returnsTrue() {
        val bloorYongeLocation = LatLng(43.6709, -79.3857)
        val result = TransitHelper.isNearMajorHub(bloorYongeLocation)
        assertTrue("Bloor-Yonge is a major hub", result)
    }

    @Test
    fun isNearMajorHub_atStGeorge_returnsTrue() {
        val stGeorgeLocation = LatLng(43.6682, -79.3998)
        val result = TransitHelper.isNearMajorHub(stGeorgeLocation)
        assertTrue("St. George is a major hub", result)
    }

    @Test
    fun isNearMajorHub_atSheppardYonge_returnsTrue() {
        val sheppardYongeLocation = LatLng(43.7610, -79.4108)
        val result = TransitHelper.isNearMajorHub(sheppardYongeLocation)
        assertTrue("Sheppard-Yonge is a major hub", result)
    }

    @Test
    fun isNearMajorHub_atMinorStation_returnsFalse() {
        // Davisville is not a major hub
        val davisvilleLocation = LatLng(43.6976, -79.3972)
        val result = TransitHelper.isNearMajorHub(davisvilleLocation)
        assertFalse("Davisville is not a major hub", result)
    }

    // ==================== Restaurant Extension Tests ====================

    @Test
    fun restaurantIsTransitAccessible_nearStation_returnsTrue() {
        val restaurant = Restaurant(
            id = "1",
            name = "Test Restaurant",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            distance = 0.1f,
            imageUrl = null,
            address = "Near Union Station",
            location = LatLng(43.6460, -79.3806) // Near Union
        )

        assertTrue(restaurant.isTransitAccessible())
    }

    @Test
    fun restaurantIsTransitAccessible_farFromStation_returnsFalse() {
        val restaurant = Restaurant(
            id = "1",
            name = "Test Restaurant",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            distance = 5.0f,
            imageUrl = null,
            address = "Far from transit",
            location = LatLng(43.5, -79.3) // In Lake Ontario
        )

        assertFalse(restaurant.isTransitAccessible())
    }

    @Test
    fun restaurantFindNearestStation_returnsStation() {
        val restaurant = Restaurant(
            id = "1",
            name = "Test Restaurant",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            distance = 0.1f,
            imageUrl = null,
            address = "Near Union Station",
            location = LatLng(43.6460, -79.3806)
        )

        val result = restaurant.findNearestStation()
        assertNotNull(result)
        assertEquals("Union", result!!.first.name)
    }
}

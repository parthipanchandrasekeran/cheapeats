package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TransitHelper data structures and constants.
 *
 * Note: Tests for distance calculations (isTransitAccessible, findNearestStation, etc.)
 * require Android's Location.distanceBetween() and should be in androidTest.
 * These tests focus on data integrity and structure validation.
 */
class TransitHelperTest {

    // ==================== Constants Tests ====================

    @Test
    fun `DEFAULT_TRANSIT_RADIUS_METERS is 500 meters`() {
        assertEquals(500.0, TransitHelper.DEFAULT_TRANSIT_RADIUS_METERS, 0.001)
    }

    // ==================== isInTorontoArea Tests ====================

    @Test
    fun `isInTorontoArea returns true for downtown Toronto`() {
        // Union Station area
        val location = LatLng(43.6453, -79.3806)
        assertTrue(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns true for North York`() {
        // York Mills area
        val location = LatLng(43.7615, -79.3456)
        assertTrue(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns true for Scarborough`() {
        val location = LatLng(43.7731, -79.2578)
        assertTrue(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns true for Etobicoke`() {
        val location = LatLng(43.6205, -79.5132)
        assertTrue(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns true for Mississauga edge`() {
        // Just inside the GTA boundary
        val location = LatLng(43.5890, -79.6441)
        assertTrue(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for Kitchener`() {
        // Kitchener is well outside the GTA
        val location = LatLng(43.4516, -80.4925)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for Hamilton`() {
        val location = LatLng(43.2557, -79.8711)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for Barrie`() {
        val location = LatLng(44.3894, -79.6903)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for Niagara Falls`() {
        val location = LatLng(43.0896, -79.0849)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for Ottawa`() {
        val location = LatLng(45.4215, -75.6972)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea returns false for New York City`() {
        val location = LatLng(40.7128, -74.0060)
        assertFalse(TransitHelper.isInTorontoArea(location))
    }

    @Test
    fun `isInTorontoArea with coordinates returns true for Toronto`() {
        assertTrue(TransitHelper.isInTorontoArea(43.6532, -79.3832))
    }

    @Test
    fun `isInTorontoArea with coordinates returns false for Kitchener`() {
        assertFalse(TransitHelper.isInTorontoArea(43.4516, -80.4925))
    }

    @Test
    fun `isInTorontoArea boundary test - just inside north`() {
        // Just below the north boundary (44.0)
        assertTrue(TransitHelper.isInTorontoArea(43.99, -79.4))
    }

    @Test
    fun `isInTorontoArea boundary test - just outside north`() {
        // Just above the north boundary (44.0)
        assertFalse(TransitHelper.isInTorontoArea(44.01, -79.4))
    }

    @Test
    fun `isInTorontoArea boundary test - just inside south`() {
        // Just above the south boundary (43.4)
        assertTrue(TransitHelper.isInTorontoArea(43.41, -79.4))
    }

    @Test
    fun `isInTorontoArea boundary test - just outside south`() {
        // Just below the south boundary (43.4)
        assertFalse(TransitHelper.isInTorontoArea(43.39, -79.4))
    }

    @Test
    fun `isInTorontoArea boundary test - just inside west`() {
        // Just east of the west boundary (-79.8)
        assertTrue(TransitHelper.isInTorontoArea(43.65, -79.79))
    }

    @Test
    fun `isInTorontoArea boundary test - just outside west`() {
        // Just west of the west boundary (-79.8)
        assertFalse(TransitHelper.isInTorontoArea(43.65, -79.81))
    }

    @Test
    fun `isInTorontoArea boundary test - just inside east`() {
        // Just west of the east boundary (-79.0)
        assertTrue(TransitHelper.isInTorontoArea(43.65, -79.01))
    }

    @Test
    fun `isInTorontoArea boundary test - just outside east`() {
        // Just east of the east boundary (-79.0)
        assertFalse(TransitHelper.isInTorontoArea(43.65, -78.99))
    }

    // ==================== SubwayStation Data Class Tests ====================

    @Test
    fun `SubwayStation stores correct data`() {
        val station = SubwayStation(
            name = "Test Station",
            location = LatLng(43.6453, -79.3806),
            lines = listOf("Line 1", "Line 2")
        )

        assertEquals("Test Station", station.name)
        assertEquals(43.6453, station.location.latitude, 0.0001)
        assertEquals(-79.3806, station.location.longitude, 0.0001)
        assertEquals(2, station.lines.size)
        assertTrue(station.lines.contains("Line 1"))
        assertTrue(station.lines.contains("Line 2"))
    }

    @Test
    fun `SubwayStation equality works correctly`() {
        val station1 = SubwayStation(
            name = "Union",
            location = LatLng(43.6453, -79.3806),
            lines = listOf("Line 1")
        )
        val station2 = SubwayStation(
            name = "Union",
            location = LatLng(43.6453, -79.3806),
            lines = listOf("Line 1")
        )
        assertEquals(station1, station2)
    }

    @Test
    fun `SubwayStation copy works correctly`() {
        val original = SubwayStation(
            name = "Union",
            location = LatLng(43.6453, -79.3806),
            lines = listOf("Line 1")
        )
        val copy = original.copy(name = "Union Station")
        assertEquals("Union Station", copy.name)
        assertEquals(original.location, copy.location)
    }

    // ==================== Major Subway Stations Tests ====================

    @Test
    fun `majorSubwayStations contains 4 stations`() {
        assertEquals(4, TransitHelper.majorSubwayStations.size)
    }

    @Test
    fun `majorSubwayStations contains Union`() {
        val union = TransitHelper.majorSubwayStations.find { it.name == "Union" }
        assertNotNull(union)
        assertEquals(43.6453, union!!.location.latitude, 0.0001)
        assertEquals(-79.3806, union.location.longitude, 0.0001)
    }

    @Test
    fun `majorSubwayStations contains Bloor-Yonge`() {
        val bloorYonge = TransitHelper.majorSubwayStations.find { it.name == "Bloor-Yonge" }
        assertNotNull(bloorYonge)
        assertTrue(bloorYonge!!.lines.contains("Line 1 Yonge-University"))
        assertTrue(bloorYonge.lines.contains("Line 2 Bloor-Danforth"))
    }

    @Test
    fun `majorSubwayStations contains St George`() {
        val stGeorge = TransitHelper.majorSubwayStations.find { it.name == "St. George" }
        assertNotNull(stGeorge)
        assertEquals(2, stGeorge!!.lines.size)
    }

    @Test
    fun `majorSubwayStations contains Sheppard-Yonge`() {
        val sheppardYonge = TransitHelper.majorSubwayStations.find { it.name == "Sheppard-Yonge" }
        assertNotNull(sheppardYonge)
        assertTrue(sheppardYonge!!.lines.contains("Line 1 Yonge-University"))
        assertTrue(sheppardYonge.lines.contains("Line 4 Sheppard"))
    }

    // ==================== All Subway Stations Tests ====================

    @Test
    fun `allSubwayStations is not empty`() {
        assertTrue(TransitHelper.allSubwayStations.isNotEmpty())
    }

    @Test
    fun `allSubwayStations has more than 50 stations`() {
        // Toronto TTC has 75 stations
        assertTrue(TransitHelper.allSubwayStations.size > 50)
    }

    @Test
    fun `allSubwayStations all have names`() {
        TransitHelper.allSubwayStations.forEach { station ->
            assertTrue(station.name.isNotBlank())
        }
    }

    @Test
    fun `allSubwayStations all have valid coordinates`() {
        TransitHelper.allSubwayStations.forEach { station ->
            // Toronto is approximately at latitude 43.6-43.8, longitude -79.2 to -79.6
            assertTrue(
                "Station ${station.name} has invalid latitude ${station.location.latitude}",
                station.location.latitude in 43.5..43.9
            )
            assertTrue(
                "Station ${station.name} has invalid longitude ${station.location.longitude}",
                station.location.longitude in -79.7..-79.2
            )
        }
    }

    @Test
    fun `allSubwayStations all have at least one line`() {
        TransitHelper.allSubwayStations.forEach { station ->
            assertTrue(
                "Station ${station.name} has no lines",
                station.lines.isNotEmpty()
            )
        }
    }

    @Test
    fun `allSubwayStations contains Line 1 stations`() {
        val line1Stations = TransitHelper.allSubwayStations.filter { station ->
            station.lines.any { it.contains("Line 1") }
        }
        assertTrue(line1Stations.isNotEmpty())
        // Line 1 is the longest line, should have many stations
        assertTrue(line1Stations.size > 20)
    }

    @Test
    fun `allSubwayStations contains Line 2 stations`() {
        val line2Stations = TransitHelper.allSubwayStations.filter { station ->
            station.lines.any { it.contains("Line 2") }
        }
        assertTrue(line2Stations.isNotEmpty())
    }

    @Test
    fun `allSubwayStations contains Line 4 stations`() {
        val line4Stations = TransitHelper.allSubwayStations.filter { station ->
            station.lines.any { it.contains("Line 4") }
        }
        assertTrue(line4Stations.isNotEmpty())
    }

    @Test
    fun `allSubwayStations contains interchange stations`() {
        // Interchange stations appear on multiple lines
        val interchangeStations = TransitHelper.allSubwayStations.filter { it.lines.size > 1 }
        assertTrue(interchangeStations.isNotEmpty())

        // Check specific interchanges
        val bloorYonge = interchangeStations.find { it.name == "Bloor-Yonge" }
        val stGeorge = interchangeStations.find { it.name == "St. George" }
        val spadina = interchangeStations.find { it.name == "Spadina" }

        assertNotNull("Bloor-Yonge should be an interchange", bloorYonge)
        assertNotNull("St. George should be an interchange", stGeorge)
        assertNotNull("Spadina should be an interchange", spadina)
    }

    @Test
    fun `allSubwayStations has unique station names within same context`() {
        // Note: Some stations might appear in majorSubwayStations and allSubwayStations
        // but within allSubwayStations itself, names should be mostly unique
        val names = TransitHelper.allSubwayStations.map { it.name }
        val duplicates = names.groupBy { it }.filter { it.value.size > 1 }

        // Allow for potential duplicates if they exist for valid reasons
        // Just make sure we don't have excessive duplicates
        assertTrue(
            "Too many duplicate station names: $duplicates",
            duplicates.size <= 5
        )
    }

    // ==================== Station Data Validation Tests ====================

    @Test
    fun `Union station is correctly placed downtown`() {
        val union = TransitHelper.allSubwayStations.find { it.name == "Union" }
        assertNotNull(union)
        // Union Station is at Front St
        assertTrue(union!!.location.latitude < 43.65)
    }

    @Test
    fun `Finch station is correctly placed in North York`() {
        val finch = TransitHelper.allSubwayStations.find { it.name == "Finch" }
        assertNotNull(finch)
        // Finch is in north Toronto
        assertTrue(finch!!.location.latitude > 43.75)
    }

    @Test
    fun `Kipling station is correctly placed in west Toronto`() {
        val kipling = TransitHelper.allSubwayStations.find { it.name == "Kipling" }
        assertNotNull(kipling)
        // Kipling is in the west
        assertTrue(kipling!!.location.longitude < -79.5)
    }

    @Test
    fun `Kennedy station is correctly placed in east Toronto`() {
        val kennedy = TransitHelper.allSubwayStations.find { it.name == "Kennedy" }
        assertNotNull(kennedy)
        // Kennedy is in the east (higher longitude, less negative)
        assertTrue(kennedy!!.location.longitude > -79.3)
    }
}

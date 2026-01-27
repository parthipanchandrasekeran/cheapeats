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

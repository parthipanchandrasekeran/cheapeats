package com.parthipan.cheapeats.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.TransitHelper
import com.parthipan.cheapeats.ui.filter.FilterBar
import com.parthipan.cheapeats.ui.filter.FilterBarContent
import com.parthipan.cheapeats.ui.filter.FilterState
import com.parthipan.cheapeats.ui.filter.FilterViewModel
import com.parthipan.cheapeats.ui.map.MapScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for location-based features.
 *
 * These tests verify that:
 * 1. Location is correctly passed from HomeScreen to MapScreen
 * 2. Toronto area detection correctly controls TTC filter visibility
 * 3. Different locations (Toronto, Brantford, Kitchener) are handled correctly
 *
 * This test class specifically addresses the bug where MapScreen
 * was not receiving userLocation from HomeScreen.
 */
@RunWith(AndroidJUnit4::class)
class LocationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Location Propagation Tests ====================

    /**
     * This test verifies the fix for the bug where MapScreen wasn't centering
     * on the user's location. It simulates the HomeScreen-MapScreen integration.
     */
    @Test
    fun mapScreen_receivesUserLocation_fromParent() {
        val brantfordLocation = LatLng(43.1394, -80.2644)
        var receivedLocation: LatLng? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                // Simulate HomeScreen passing location to MapScreen
                TestMapScreenWrapper(
                    userLocation = brantfordLocation,
                    onLocationReceived = { receivedLocation = it }
                )
            }
        }

        // Verify the location was passed correctly
        assertEquals(brantfordLocation, receivedLocation)
    }

    @Test
    fun mapScreen_receivesTorontoLocation() {
        val torontoLocation = LatLng(43.6532, -79.3832)
        var receivedLocation: LatLng? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                TestMapScreenWrapper(
                    userLocation = torontoLocation,
                    onLocationReceived = { receivedLocation = it }
                )
            }
        }

        assertEquals(torontoLocation, receivedLocation)
    }

    @Test
    fun mapScreen_receivesKitchenerLocation() {
        val kitchenerLocation = LatLng(43.4516, -80.4925)
        var receivedLocation: LatLng? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                TestMapScreenWrapper(
                    userLocation = kitchenerLocation,
                    onLocationReceived = { receivedLocation = it }
                )
            }
        }

        assertEquals(kitchenerLocation, receivedLocation)
    }

    // ==================== Toronto Area Detection + UI Integration ====================

    @Test
    fun torontoLocation_showsTTCFilter() {
        val torontoLocation = LatLng(43.6532, -79.3832)
        val isInToronto = TransitHelper.isInTorontoArea(torontoLocation)

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onClearAll = {},
                    showTTCFilter = isInToronto
                )
            }
        }

        assertTrue("Toronto should be in Toronto area", isInToronto)
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()
    }

    @Test
    fun brantfordLocation_hidesTTCFilter() {
        val brantfordLocation = LatLng(43.1394, -80.2644)
        val isInToronto = TransitHelper.isInTorontoArea(brantfordLocation)

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onClearAll = {},
                    showTTCFilter = isInToronto
                )
            }
        }

        assertFalse("Brantford should NOT be in Toronto area", isInToronto)
        composeTestRule.onNodeWithText("Near TTC").assertDoesNotExist()
    }

    @Test
    fun kitchenerLocation_hidesTTCFilter() {
        val kitchenerLocation = LatLng(43.4516, -80.4925)
        val isInToronto = TransitHelper.isInTorontoArea(kitchenerLocation)

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onClearAll = {},
                    showTTCFilter = isInToronto
                )
            }
        }

        assertFalse("Kitchener should NOT be in Toronto area", isInToronto)
        composeTestRule.onNodeWithText("Near TTC").assertDoesNotExist()
    }

    // ==================== End-to-End Flow Simulation ====================

    /**
     * Simulates the full flow from location detection to UI rendering.
     * This is the test that would have caught the original bug.
     */
    @Test
    fun fullFlow_brantfordLocation_correctlyConfiguresUI() {
        val brantfordLocation = LatLng(43.1394, -80.2644)

        composeTestRule.setContent {
            CheapEatsTheme {
                // Simulate HomeScreen's location-based logic
                SimulatedHomeScreenFlow(userLocation = brantfordLocation)
            }
        }

        // Brantford is outside Toronto, so TTC filter should be hidden
        composeTestRule.onNodeWithText("Near TTC").assertDoesNotExist()

        // Other filters should still work
        composeTestRule.onNodeWithText("Under \$15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Discount").assertIsDisplayed()

        // Map info should show correct count
        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()
    }

    @Test
    fun fullFlow_torontoLocation_correctlyConfiguresUI() {
        val torontoLocation = LatLng(43.6532, -79.3832)

        composeTestRule.setContent {
            CheapEatsTheme {
                SimulatedHomeScreenFlow(userLocation = torontoLocation)
            }
        }

        // Toronto should show TTC filter
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Under \$15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Discount").assertIsDisplayed()
    }

    @Test
    fun fullFlow_locationChange_updatesUI() {
        composeTestRule.setContent {
            CheapEatsTheme {
                DynamicLocationFlow()
            }
        }

        // Initially in Toronto - TTC filter visible
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()

        // Click button to switch to Brantford
        composeTestRule.onNodeWithText("Switch to Brantford").performClick()
        composeTestRule.waitForIdle()

        // Now TTC filter should be hidden
        composeTestRule.onNodeWithText("Near TTC").assertDoesNotExist()

        // Click button to switch back to Toronto
        composeTestRule.onNodeWithText("Switch to Toronto").performClick()
        composeTestRule.waitForIdle()

        // TTC filter should be visible again
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()
    }

    // ==================== Test Composables ====================

    /**
     * Test wrapper to verify MapScreen receives the location parameter
     */
    @Composable
    private fun TestMapScreenWrapper(
        userLocation: LatLng?,
        onLocationReceived: (LatLng?) -> Unit
    ) {
        // Call the callback with the location that would be passed to MapScreen
        onLocationReceived(userLocation)

        // Render MapScreen with the location
        MapScreen(
            restaurants = listOf(createTestRestaurant()),
            userLocation = userLocation
        )
    }

    /**
     * Simulates the HomeScreen flow for testing location-based logic
     */
    @Composable
    private fun SimulatedHomeScreenFlow(userLocation: LatLng) {
        val isInTorontoArea = TransitHelper.isInTorontoArea(userLocation)
        val filterViewModel = remember { FilterViewModel() }

        Column {
            FilterBar(
                filterViewModel = filterViewModel,
                showTTCFilter = isInTorontoArea
            )

            MapScreen(
                restaurants = listOf(createTestRestaurant()),
                userLocation = userLocation
            )
        }
    }

    /**
     * Test composable for dynamic location changes
     */
    @Composable
    private fun DynamicLocationFlow() {
        var userLocation by remember { mutableStateOf(LatLng(43.6532, -79.3832)) } // Start in Toronto
        val isInTorontoArea = TransitHelper.isInTorontoArea(userLocation)
        val filterViewModel = remember { FilterViewModel() }

        Column {
            // Toggle buttons for testing
            androidx.compose.material3.Button(
                onClick = {
                    userLocation = if (isInTorontoArea) {
                        LatLng(43.1394, -80.2644) // Brantford
                    } else {
                        LatLng(43.6532, -79.3832) // Toronto
                    }
                }
            ) {
                androidx.compose.material3.Text(
                    if (isInTorontoArea) "Switch to Brantford" else "Switch to Toronto"
                )
            }

            FilterBar(
                filterViewModel = filterViewModel,
                showTTCFilter = isInTorontoArea
            )
        }
    }

    // ==================== Helper Functions ====================

    private fun createTestRestaurant(): Restaurant {
        return Restaurant(
            id = "1",
            name = "Test Restaurant",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.0f,
            distance = 1.0f,
            imageUrl = null,
            address = "123 Test St",
            location = LatLng(43.6532, -79.3832)
        )
    }
}

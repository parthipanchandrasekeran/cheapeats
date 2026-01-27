package com.parthipan.cheapeats.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.ui.map.MapScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for MapScreen component.
 *
 * These tests verify that:
 * 1. MapScreen correctly receives and uses userLocation
 * 2. Restaurant data is properly displayed on the map
 * 3. Legend overlay shows correct information
 *
 * Note: Full map rendering tests require a device with Google Play Services.
 * These tests focus on the UI elements that can be tested without the map.
 */
@RunWith(AndroidJUnit4::class)
class MapScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Legend Display Tests ====================

    @Test
    fun mapScreen_showsLegend() {
        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = emptyList(),
                    userLocation = LatLng(43.6532, -79.3832)
                )
            }
        }

        // Legend should show sponsored and standard markers
        composeTestRule.onNodeWithText("Sponsored").assertIsDisplayed()
        composeTestRule.onNodeWithText("Standard").assertIsDisplayed()
    }

    @Test
    fun mapScreen_showsRestaurantCount_withUserLocation() {
        val restaurants = listOf(
            createTestRestaurant("1", "Restaurant 1", isSponsored = true),
            createTestRestaurant("2", "Restaurant 2", isSponsored = false),
            createTestRestaurant("3", "Restaurant 3", isSponsored = true)
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = restaurants,
                    userLocation = LatLng(43.6532, -79.3832) // Toronto location
                )
            }
        }

        // Should show restaurant count with sponsored count
        composeTestRule.onNodeWithText("3 restaurants (2 sponsored)").assertIsDisplayed()
    }

    @Test
    fun mapScreen_showsLocationPrompt_whenNoUserLocation() {
        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = emptyList(),
                    userLocation = null // No location yet
                )
            }
        }

        // Should prompt to enable location
        composeTestRule.onNodeWithText("Enable location for nearby places").assertExists()
    }

    // ==================== Restaurant Count Tests ====================

    @Test
    fun mapScreen_countsSponsored_correctly() {
        val restaurants = listOf(
            createTestRestaurant("1", "R1", isSponsored = true),
            createTestRestaurant("2", "R2", isSponsored = true),
            createTestRestaurant("3", "R3", isSponsored = true),
            createTestRestaurant("4", "R4", isSponsored = false),
            createTestRestaurant("5", "R5", isSponsored = false)
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = restaurants,
                    userLocation = LatLng(43.6532, -79.3832)
                )
            }
        }

        // 5 restaurants, 3 sponsored
        composeTestRule.onNodeWithText("5 restaurants (3 sponsored)").assertIsDisplayed()
    }

    @Test
    fun mapScreen_countsZeroSponsored() {
        val restaurants = listOf(
            createTestRestaurant("1", "R1", isSponsored = false),
            createTestRestaurant("2", "R2", isSponsored = false)
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = restaurants,
                    userLocation = LatLng(43.6532, -79.3832)
                )
            }
        }

        composeTestRule.onNodeWithText("2 restaurants (0 sponsored)").assertIsDisplayed()
    }

    // ==================== Location Parameter Tests ====================

    @Test
    fun mapScreen_acceptsTorontoLocation() {
        // Test that MapScreen accepts a Toronto location
        val torontoLocation = LatLng(43.6532, -79.3832)

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = listOf(createTestRestaurant("1", "Test")),
                    userLocation = torontoLocation
                )
            }
        }

        // Should show restaurant count (not the location prompt)
        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()
    }

    @Test
    fun mapScreen_acceptsBrantfordLocation() {
        // Test that MapScreen accepts a Brantford location (outside Toronto)
        val brantfordLocation = LatLng(43.1394, -80.2644)

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = listOf(createTestRestaurant("1", "Brantford Place")),
                    userLocation = brantfordLocation
                )
            }
        }

        // Should show restaurant count (not the location prompt)
        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()
    }

    @Test
    fun mapScreen_acceptsKitchenerLocation() {
        // Test that MapScreen accepts a Kitchener location
        val kitchenerLocation = LatLng(43.4516, -80.4925)

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = listOf(createTestRestaurant("1", "Kitchener Eats")),
                    userLocation = kitchenerLocation
                )
            }
        }

        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()
    }

    // ==================== Dynamic Update Tests ====================

    @Test
    fun mapScreen_updatesCount_whenRestaurantsChange() {
        var restaurants by mutableStateOf(listOf(createTestRestaurant("1", "R1")))

        composeTestRule.setContent {
            CheapEatsTheme {
                MapScreen(
                    restaurants = restaurants,
                    userLocation = LatLng(43.6532, -79.3832)
                )
            }
        }

        // Initially 1 restaurant
        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()

        // Add more restaurants
        restaurants = listOf(
            createTestRestaurant("1", "R1"),
            createTestRestaurant("2", "R2"),
            createTestRestaurant("3", "R3", isSponsored = true)
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("3 restaurants (1 sponsored)").assertIsDisplayed()
    }

    // ==================== Helper Functions ====================

    private fun createTestRestaurant(
        id: String,
        name: String,
        isSponsored: Boolean = false
    ): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.0f,
            distance = 1.0f,
            imageUrl = null,
            address = "123 Test St",
            location = LatLng(43.6532, -79.3832),
            isSponsored = isSponsored
        )
    }
}

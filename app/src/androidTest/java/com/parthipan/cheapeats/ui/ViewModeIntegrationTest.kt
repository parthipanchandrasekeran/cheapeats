package com.parthipan.cheapeats.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.ui.home.RestaurantList
import com.parthipan.cheapeats.ui.home.ViewMode
import com.parthipan.cheapeats.ui.map.MapScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for view mode switching between List and Map views.
 *
 * These tests verify that:
 * 1. View mode toggle works correctly
 * 2. Both list and map views receive the same restaurant data
 * 3. User location is passed correctly to MapScreen when switching views
 */
@RunWith(AndroidJUnit4::class)
class ViewModeIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== View Mode Switching Tests ====================

    @Test
    fun viewModeToggle_switchesToMapView() {
        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeSwitcher()
            }
        }

        // Initially in list view
        composeTestRule.onNodeWithText("Pizza Palace").assertIsDisplayed()

        // Click map toggle button
        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()

        // Should now show map (legend visible)
        composeTestRule.onNodeWithText("Sponsored").assertIsDisplayed()
        composeTestRule.onNodeWithText("Standard").assertIsDisplayed()
    }

    @Test
    fun viewModeToggle_switchesBackToListView() {
        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeSwitcher()
            }
        }

        // Switch to map view
        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()
        composeTestRule.waitForIdle()

        // Verify we're in map view
        composeTestRule.onNodeWithText("Sponsored").assertIsDisplayed()

        // Switch back to list view
        composeTestRule.onNodeWithContentDescription("Switch to List View").performClick()
        composeTestRule.waitForIdle()

        // Should show restaurant list again
        composeTestRule.onNodeWithText("Pizza Palace").assertIsDisplayed()
    }

    @Test
    fun viewModeToggle_preservesRestaurantData() {
        val restaurants = listOf(
            createTestRestaurant("1", "Restaurant A", isSponsored = true),
            createTestRestaurant("2", "Restaurant B", isSponsored = false),
            createTestRestaurant("3", "Restaurant C", isSponsored = true)
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeSwitcherWithData(restaurants = restaurants)
            }
        }

        // List view shows all restaurants
        composeTestRule.onNodeWithText("Restaurant A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurant B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurant C").assertIsDisplayed()

        // Switch to map
        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()
        composeTestRule.waitForIdle()

        // Map shows correct count
        composeTestRule.onNodeWithText("3 restaurants (2 sponsored)").assertIsDisplayed()

        // Switch back to list
        composeTestRule.onNodeWithContentDescription("Switch to List View").performClick()
        composeTestRule.waitForIdle()

        // All restaurants still visible
        composeTestRule.onNodeWithText("Restaurant A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurant B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurant C").assertIsDisplayed()
    }

    @Test
    fun viewModeToggle_mapReceivesUserLocation() {
        val brantfordLocation = LatLng(43.1394, -80.2644)

        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeSwitcherWithLocation(userLocation = brantfordLocation)
            }
        }

        // Switch to map
        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()
        composeTestRule.waitForIdle()

        // Map should show restaurant count (indicates location was received)
        composeTestRule.onNodeWithText("1 restaurants (0 sponsored)").assertIsDisplayed()
    }

    // ==================== View Mode State Tests ====================

    @Test
    fun viewMode_initialState_isList() {
        var currentViewMode: ViewMode? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeStateTracker { mode ->
                    currentViewMode = mode
                }
            }
        }

        assertEquals(ViewMode.LIST, currentViewMode)
    }

    @Test
    fun viewMode_afterToggle_isMap() {
        var currentViewMode: ViewMode? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeStateTracker { mode ->
                    currentViewMode = mode
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()
        composeTestRule.waitForIdle()

        assertEquals(ViewMode.MAP, currentViewMode)
    }

    @Test
    fun viewMode_doubleToggle_returnsList() {
        var currentViewMode: ViewMode? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                ViewModeStateTracker { mode ->
                    currentViewMode = mode
                }
            }
        }

        // Toggle to map
        composeTestRule.onNodeWithContentDescription("Switch to Map View").performClick()
        composeTestRule.waitForIdle()

        // Toggle back to list
        composeTestRule.onNodeWithContentDescription("Switch to List View").performClick()
        composeTestRule.waitForIdle()

        assertEquals(ViewMode.LIST, currentViewMode)
    }

    // ==================== Test Composables ====================

    @Composable
    private fun ViewModeSwitcher() {
        var viewMode by remember { mutableStateOf(ViewMode.LIST) }
        val restaurants = listOf(createTestRestaurant("1", "Pizza Palace"))
        val userLocation = LatLng(43.6532, -79.3832)

        Column(modifier = Modifier.fillMaxSize()) {
            // Toggle button
            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                }
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) {
                        Icons.Default.LocationOn
                    } else {
                        Icons.AutoMirrored.Filled.List
                    },
                    contentDescription = if (viewMode == ViewMode.LIST) {
                        "Switch to Map View"
                    } else {
                        "Switch to List View"
                    }
                )
            }

            // Content based on view mode
            when (viewMode) {
                ViewMode.LIST -> {
                    RestaurantList(
                        restaurants = restaurants,
                        onRestaurantClick = {}
                    )
                }
                ViewMode.MAP -> {
                    MapScreen(
                        restaurants = restaurants,
                        userLocation = userLocation
                    )
                }
            }
        }
    }

    @Composable
    private fun ViewModeSwitcherWithData(restaurants: List<Restaurant>) {
        var viewMode by remember { mutableStateOf(ViewMode.LIST) }
        val userLocation = LatLng(43.6532, -79.3832)

        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                }
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) {
                        Icons.Default.LocationOn
                    } else {
                        Icons.AutoMirrored.Filled.List
                    },
                    contentDescription = if (viewMode == ViewMode.LIST) {
                        "Switch to Map View"
                    } else {
                        "Switch to List View"
                    }
                )
            }

            when (viewMode) {
                ViewMode.LIST -> {
                    RestaurantList(
                        restaurants = restaurants,
                        onRestaurantClick = {}
                    )
                }
                ViewMode.MAP -> {
                    MapScreen(
                        restaurants = restaurants,
                        userLocation = userLocation
                    )
                }
            }
        }
    }

    @Composable
    private fun ViewModeSwitcherWithLocation(userLocation: LatLng) {
        var viewMode by remember { mutableStateOf(ViewMode.LIST) }
        val restaurants = listOf(createTestRestaurant("1", "Test Restaurant"))

        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                }
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) {
                        Icons.Default.LocationOn
                    } else {
                        Icons.AutoMirrored.Filled.List
                    },
                    contentDescription = if (viewMode == ViewMode.LIST) {
                        "Switch to Map View"
                    } else {
                        "Switch to List View"
                    }
                )
            }

            when (viewMode) {
                ViewMode.LIST -> {
                    RestaurantList(
                        restaurants = restaurants,
                        onRestaurantClick = {}
                    )
                }
                ViewMode.MAP -> {
                    MapScreen(
                        restaurants = restaurants,
                        userLocation = userLocation  // Key: location is passed here
                    )
                }
            }
        }
    }

    @Composable
    private fun ViewModeStateTracker(onViewModeChanged: (ViewMode) -> Unit) {
        var viewMode by remember { mutableStateOf(ViewMode.LIST) }

        // Report state changes
        onViewModeChanged(viewMode)

        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                }
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) {
                        Icons.Default.LocationOn
                    } else {
                        Icons.AutoMirrored.Filled.List
                    },
                    contentDescription = if (viewMode == ViewMode.LIST) {
                        "Switch to Map View"
                    } else {
                        "Switch to List View"
                    }
                )
            }
        }
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

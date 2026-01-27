package com.parthipan.cheapeats.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.ui.filter.FilterBarContent
import com.parthipan.cheapeats.ui.filter.FilterState
import com.parthipan.cheapeats.ui.filter.FilterType
import com.parthipan.cheapeats.ui.home.RestaurantCard
import com.parthipan.cheapeats.ui.home.RestaurantList
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for HomeScreen and its child components.
 *
 * These tests verify that:
 * 1. Components are correctly wired together
 * 2. Data flows correctly between parent and child components
 * 3. User interactions trigger the expected callbacks
 * 4. Conditional rendering works as expected
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== FilterBar Integration Tests ====================

    @Test
    fun filterBar_showsTTCFilter_whenInTorontoArea() {
        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = true // In Toronto
                )
            }
        }

        // TTC filter should be visible
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()
    }

    @Test
    fun filterBar_hidesTTCFilter_whenOutsideTorontoArea() {
        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = false // Outside Toronto (e.g., Brantford)
                )
            }
        }

        // TTC filter should NOT be visible
        composeTestRule.onNodeWithText("Near TTC").assertDoesNotExist()
    }

    @Test
    fun filterBar_alwaysShowsUnder15AndStudentDiscount() {
        // Test with showTTCFilter = false
        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = false
                )
            }
        }

        // Under $15 and Student Discount should always be visible
        composeTestRule.onNodeWithText("Under \$15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Discount").assertIsDisplayed()
    }

    @Test
    fun filterBar_toggleCallback_receivesCorrectFilterType() {
        var toggledFilter: FilterType? = null

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(),
                    onFilterToggle = { filterType -> toggledFilter = filterType },
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = true
                )
            }
        }

        // Click on Under $15 filter
        composeTestRule.onNodeWithText("Under \$15").performClick()
        assertEquals(FilterType.UNDER_15, toggledFilter)

        // Click on Student Discount filter
        composeTestRule.onNodeWithText("Student Discount").performClick()
        assertEquals(FilterType.STUDENT_DISCOUNT, toggledFilter)

        // Click on Near TTC filter
        composeTestRule.onNodeWithText("Near TTC").performClick()
        assertEquals(FilterType.NEAR_TTC, toggledFilter)
    }

    @Test
    fun filterBar_clearAllButton_visibleWhenFiltersActive() {
        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(isUnder15Active = true),
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = true
                )
            }
        }

        // Clear button should be visible when filters are active
        composeTestRule.onNodeWithContentDescription("Clear all filters").assertIsDisplayed()
    }

    @Test
    fun filterBar_clearAllButton_hiddenWhenNoFiltersActive() {
        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(), // No active filters
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = {},
                    showTTCFilter = true
                )
            }
        }

        // Clear button should NOT be visible when no filters are active
        composeTestRule.onNodeWithContentDescription("Clear all filters").assertDoesNotExist()
    }

    @Test
    fun filterBar_clearAllCallback_isCalled() {
        var clearAllCalled = false

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBarContent(
                    filterState = FilterState(isUnder15Active = true),
                    onFilterToggle = {},
                    onPriceModeChange = {},
                    onClearAll = { clearAllCalled = true },
                    showTTCFilter = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Clear all filters").performClick()
        assertTrue("Clear all callback should be called", clearAllCalled)
    }

    // ==================== RestaurantList Integration Tests ====================

    @Test
    fun restaurantList_displaysRestaurants() {
        val restaurants = listOf(
            createTestRestaurant("1", "Pizza Palace"),
            createTestRestaurant("2", "Burger Barn"),
            createTestRestaurant("3", "Sushi Spot")
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantList(
                    restaurants = restaurants,
                    onRestaurantClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Pizza Palace").assertIsDisplayed()
        composeTestRule.onNodeWithText("Burger Barn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sushi Spot").assertIsDisplayed()
    }

    @Test
    fun restaurantList_emptyState_showsMessage() {
        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantList(
                    restaurants = emptyList(),
                    onRestaurantClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No restaurants found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try adjusting your filters").assertIsDisplayed()
    }

    @Test
    fun restaurantList_clickCallback_receivesCorrectRestaurant() {
        var clickedRestaurant: Restaurant? = null
        val restaurant = createTestRestaurant("1", "Pizza Palace")

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantList(
                    restaurants = listOf(restaurant),
                    onRestaurantClick = { clickedRestaurant = it },
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Pizza Palace").performClick()
        assertEquals("1", clickedRestaurant?.id)
        assertEquals("Pizza Palace", clickedRestaurant?.name)
    }

    // ==================== RestaurantCard Integration Tests ====================

    @Test
    fun restaurantCard_displaysAllInfo() {
        val restaurant = Restaurant(
            id = "1",
            name = "Test Restaurant",
            cuisine = "Italian",
            priceLevel = 2,
            rating = 4.5f,
            distance = 1.2f,
            imageUrl = null,
            address = "123 Test St",
            location = LatLng(43.6532, -79.3832),
            hasStudentDiscount = true,
            nearTTC = true
        )

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Restaurant").assertIsDisplayed()
        composeTestRule.onNodeWithText("Italian").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student").assertIsDisplayed()
        composeTestRule.onNodeWithText("TTC").assertIsDisplayed()
    }

    @Test
    fun restaurantCard_sponsoredBadge_displayed() {
        val restaurant = createTestRestaurant("1", "Sponsored Place", isSponsored = true)

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("AD").assertIsDisplayed()
    }

    @Test
    fun restaurantCard_sponsoredBadge_notDisplayedForNonSponsored() {
        val restaurant = createTestRestaurant("1", "Regular Place", isSponsored = false)

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("AD").assertDoesNotExist()
    }

    @Test
    fun restaurantCard_clickCallback_isCalled() {
        var clickCalled = false

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = createTestRestaurant("1", "Click Me"),
                    onClick = { clickCalled = true },
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Click Me").performClick()
        assertTrue("Click callback should be called", clickCalled)
    }

    @Test
    fun restaurantCard_hidesStudentChip_whenNoDiscount() {
        val restaurant = createTestRestaurant("1", "No Discount", hasStudentDiscount = false)

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Student").assertDoesNotExist()
    }

    @Test
    fun restaurantCard_hidesTTCChip_whenNotNearTTC() {
        val restaurant = createTestRestaurant("1", "Far from TTC", nearTTC = false)

        composeTestRule.setContent {
            CheapEatsTheme {
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("TTC").assertDoesNotExist()
    }

    // ==================== Helper Functions ====================

    private fun createTestRestaurant(
        id: String,
        name: String,
        cuisine: String = "Italian",
        priceLevel: Int = 2,
        rating: Float = 4.0f,
        distance: Float = 1.0f,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false,
        isSponsored: Boolean = false
    ): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            cuisine = cuisine,
            priceLevel = priceLevel,
            rating = rating,
            distance = distance,
            imageUrl = null,
            address = "123 Test St",
            location = LatLng(43.6532, -79.3832),
            hasStudentDiscount = hasStudentDiscount,
            nearTTC = nearTTC,
            isSponsored = isSponsored
        )
    }
}

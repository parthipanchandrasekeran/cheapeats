package com.parthipan.cheapeats.ui.filter

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for FilterBar with FilterViewModel.
 *
 * These tests verify that:
 * 1. FilterBar correctly connects to FilterViewModel
 * 2. Filter toggles update the ViewModel state
 * 3. Clear all resets all filters
 * 4. Filter state is correctly observed and rendered
 */
@RunWith(AndroidJUnit4::class)
class FilterIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== ViewModel Integration Tests ====================

    @Test
    fun filterBar_connectsToViewModel_displaysInitialState() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = true
                )
            }
        }

        // All filters should be displayed but not selected initially
        composeTestRule.onNodeWithText("Under \$15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Discount").assertIsDisplayed()
        composeTestRule.onNodeWithText("Near TTC").assertIsDisplayed()

        // Clear button should not be visible (no active filters)
        composeTestRule.onNodeWithContentDescription("Clear all filters").assertDoesNotExist()
    }

    @Test
    fun filterBar_toggleFilter_updatesViewModel() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = true
                )
            }
        }

        // Initially no filters active
        assertFalse(viewModel.filterState.value.isUnder15Active)

        // Click Under $15
        composeTestRule.onNodeWithText("Under \$15").performClick()

        // ViewModel should be updated
        assertTrue(viewModel.filterState.value.isUnder15Active)
    }

    @Test
    fun filterBar_toggleMultipleFilters_updatesViewModel() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = true
                )
            }
        }

        // Toggle all three filters
        composeTestRule.onNodeWithText("Under \$15").performClick()
        composeTestRule.onNodeWithText("Student Discount").performClick()
        composeTestRule.onNodeWithText("Near TTC").performClick()

        // All should be active
        val state = viewModel.filterState.value
        assertTrue(state.isUnder15Active)
        assertTrue(state.isStudentDiscountActive)
        assertTrue(state.isNearTTCActive)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun filterBar_clearAll_resetsViewModel() {
        val viewModel = FilterViewModel()
        // Pre-set some filters
        viewModel.toggleFilter(FilterType.UNDER_15)
        viewModel.toggleFilter(FilterType.STUDENT_DISCOUNT)

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = true
                )
            }
        }

        // Verify filters are active
        assertTrue(viewModel.filterState.value.hasActiveFilters)

        // Click clear all
        composeTestRule.onNodeWithContentDescription("Clear all filters").performClick()

        // All filters should be reset
        val state = viewModel.filterState.value
        assertFalse(state.isUnder15Active)
        assertFalse(state.isStudentDiscountActive)
        assertFalse(state.isNearTTCActive)
        assertFalse(state.hasActiveFilters)
    }

    @Test
    fun filterBar_doubleToggle_deactivatesFilter() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = true
                )
            }
        }

        // Toggle on
        composeTestRule.onNodeWithText("Under \$15").performClick()
        assertTrue(viewModel.filterState.value.isUnder15Active)

        // Toggle off
        composeTestRule.onNodeWithText("Under \$15").performClick()
        assertFalse(viewModel.filterState.value.isUnder15Active)
    }

    // ==================== Filter Application Tests ====================

    @Test
    fun applyFilters_under15_filtersCorrectly() {
        val restaurants = listOf(
            createRestaurantWithPrice("1", "Cheap Place", 10.0f),
            createRestaurantWithPrice("2", "Expensive Place", 25.0f),
            createRestaurantWithPrice("3", "Budget Spot", 12.0f)
        )

        val filterState = FilterState(isUnder15Active = true)
        val filtered = FilterViewModel.applyFilters(restaurants, filterState)

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.name == "Cheap Place" })
        assertTrue(filtered.any { it.name == "Budget Spot" })
        assertFalse(filtered.any { it.name == "Expensive Place" })
    }

    @Test
    fun applyFilters_studentDiscount_filtersCorrectly() {
        val restaurants = listOf(
            createRestaurant("1", "With Discount", hasStudentDiscount = true),
            createRestaurant("2", "No Discount", hasStudentDiscount = false),
            createRestaurant("3", "Also Discount", hasStudentDiscount = true)
        )

        val filterState = FilterState(isStudentDiscountActive = true)
        val filtered = FilterViewModel.applyFilters(restaurants, filterState)

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.hasStudentDiscount })
    }

    @Test
    fun applyFilters_nearTTC_filtersCorrectly() {
        val restaurants = listOf(
            createRestaurant("1", "Near Transit", nearTTC = true),
            createRestaurant("2", "Far from Transit", nearTTC = false),
            createRestaurant("3", "Also Near", nearTTC = true)
        )

        val filterState = FilterState(isNearTTCActive = true)
        val filtered = FilterViewModel.applyFilters(restaurants, filterState)

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.nearTTC })
    }

    @Test
    fun applyFilters_multipleFilters_combinesWithAnd() {
        val restaurants = listOf(
            createRestaurantWithPrice("1", "Cheap with Discount", 10.0f, hasStudentDiscount = true),
            createRestaurantWithPrice("2", "Cheap no Discount", 10.0f, hasStudentDiscount = false),
            createRestaurantWithPrice("3", "Expensive with Discount", 25.0f, hasStudentDiscount = true)
        )

        val filterState = FilterState(
            isUnder15Active = true,
            isStudentDiscountActive = true
        )
        val filtered = FilterViewModel.applyFilters(restaurants, filterState)

        // Only restaurant that is both cheap AND has student discount
        assertEquals(1, filtered.size)
        assertEquals("Cheap with Discount", filtered.first().name)
    }

    @Test
    fun applyFilters_noFilters_returnsAll() {
        val restaurants = listOf(
            createRestaurant("1", "R1"),
            createRestaurant("2", "R2"),
            createRestaurant("3", "R3")
        )

        val filterState = FilterState() // No active filters
        val filtered = FilterViewModel.applyFilters(restaurants, filterState)

        assertEquals(3, filtered.size)
    }

    // ==================== TTC Filter Visibility Tests ====================

    @Test
    fun filterBar_hideTTCFilter_doesNotAffectOtherFilters() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = false // Hide TTC filter (outside Toronto)
                )
            }
        }

        // Can still toggle other filters
        composeTestRule.onNodeWithText("Under \$15").performClick()
        composeTestRule.onNodeWithText("Student Discount").performClick()

        assertTrue(viewModel.filterState.value.isUnder15Active)
        assertTrue(viewModel.filterState.value.isStudentDiscountActive)
    }

    @Test
    fun filterState_hasActiveFilters_excludesTTC_whenHidden() {
        val viewModel = FilterViewModel()

        composeTestRule.setContent {
            CheapEatsTheme {
                FilterBar(
                    filterViewModel = viewModel,
                    showTTCFilter = false
                )
            }
        }

        // No visible filters selected
        assertFalse(viewModel.filterState.value.hasActiveFilters)

        // Select Under $15
        composeTestRule.onNodeWithText("Under \$15").performClick()
        assertTrue(viewModel.filterState.value.hasActiveFilters)

        // Clear all should work
        composeTestRule.onNodeWithContentDescription("Clear all filters").performClick()
        assertFalse(viewModel.filterState.value.hasActiveFilters)
    }

    // ==================== Helper Functions ====================

    private fun createRestaurant(
        id: String,
        name: String,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false
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
            hasStudentDiscount = hasStudentDiscount,
            nearTTC = nearTTC
        )
    }

    private fun createRestaurantWithPrice(
        id: String,
        name: String,
        averagePrice: Float,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false
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
            hasStudentDiscount = hasStudentDiscount,
            nearTTC = nearTTC,
            averagePrice = averagePrice
        )
    }
}

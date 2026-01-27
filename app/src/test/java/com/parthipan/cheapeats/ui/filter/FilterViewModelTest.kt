package com.parthipan.cheapeats.ui.filter

import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FilterState data class.
 */
class FilterStateTest {

    @Test
    fun `default FilterState has no active filters`() {
        val state = FilterState()
        assertFalse(state.isUnder15Active)
        assertFalse(state.isStudentDiscountActive)
        assertFalse(state.isNearTTCActive)
    }

    @Test
    fun `hasActiveFilters returns false when no filters active`() {
        val state = FilterState()
        assertFalse(state.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when Under15 is active`() {
        val state = FilterState(isUnder15Active = true)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when StudentDiscount is active`() {
        val state = FilterState(isStudentDiscountActive = true)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when NearTTC is active`() {
        val state = FilterState(isNearTTCActive = true)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `hasActiveFilters returns true when multiple filters active`() {
        val state = FilterState(isUnder15Active = true, isNearTTCActive = true)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `activeFilterCount returns 0 when no filters active`() {
        val state = FilterState()
        assertEquals(0, state.activeFilterCount)
    }

    @Test
    fun `activeFilterCount returns 1 when one filter active`() {
        val state = FilterState(isUnder15Active = true)
        assertEquals(1, state.activeFilterCount)
    }

    @Test
    fun `activeFilterCount returns 2 when two filters active`() {
        val state = FilterState(isUnder15Active = true, isStudentDiscountActive = true)
        assertEquals(2, state.activeFilterCount)
    }

    @Test
    fun `activeFilterCount returns 3 when all filters active`() {
        val state = FilterState(isUnder15Active = true, isStudentDiscountActive = true, isNearTTCActive = true)
        assertEquals(3, state.activeFilterCount)
    }
}

/**
 * Unit tests for FilterViewModel.
 */
class FilterViewModelTest {

    private lateinit var viewModel: FilterViewModel

    @Before
    fun setup() {
        viewModel = FilterViewModel()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial filter state has no active filters`() {
        val state = viewModel.filterState.value
        assertFalse(state.hasActiveFilters)
    }

    // ==================== toggleFilter Tests ====================

    @Test
    fun `toggleFilter enables Under15 filter`() {
        viewModel.toggleFilter(FilterType.UNDER_15)
        assertTrue(viewModel.filterState.value.isUnder15Active)
    }

    @Test
    fun `toggleFilter disables Under15 filter when already active`() {
        viewModel.toggleFilter(FilterType.UNDER_15)
        viewModel.toggleFilter(FilterType.UNDER_15)
        assertFalse(viewModel.filterState.value.isUnder15Active)
    }

    @Test
    fun `toggleFilter enables StudentDiscount filter`() {
        viewModel.toggleFilter(FilterType.STUDENT_DISCOUNT)
        assertTrue(viewModel.filterState.value.isStudentDiscountActive)
    }

    @Test
    fun `toggleFilter enables NearTTC filter`() {
        viewModel.toggleFilter(FilterType.NEAR_TTC)
        assertTrue(viewModel.filterState.value.isNearTTCActive)
    }

    @Test
    fun `toggling multiple filters works independently`() {
        viewModel.toggleFilter(FilterType.UNDER_15)
        viewModel.toggleFilter(FilterType.NEAR_TTC)

        val state = viewModel.filterState.value
        assertTrue(state.isUnder15Active)
        assertFalse(state.isStudentDiscountActive)
        assertTrue(state.isNearTTCActive)
    }

    // ==================== setFilter Tests ====================

    @Test
    fun `setFilter activates Under15 filter`() {
        viewModel.setFilter(FilterType.UNDER_15, true)
        assertTrue(viewModel.filterState.value.isUnder15Active)
    }

    @Test
    fun `setFilter deactivates Under15 filter`() {
        viewModel.toggleFilter(FilterType.UNDER_15)
        viewModel.setFilter(FilterType.UNDER_15, false)
        assertFalse(viewModel.filterState.value.isUnder15Active)
    }

    @Test
    fun `setFilter activates StudentDiscount filter`() {
        viewModel.setFilter(FilterType.STUDENT_DISCOUNT, true)
        assertTrue(viewModel.filterState.value.isStudentDiscountActive)
    }

    @Test
    fun `setFilter activates NearTTC filter`() {
        viewModel.setFilter(FilterType.NEAR_TTC, true)
        assertTrue(viewModel.filterState.value.isNearTTCActive)
    }

    // ==================== clearAllFilters Tests ====================

    @Test
    fun `clearAllFilters resets all filters`() {
        viewModel.toggleFilter(FilterType.UNDER_15)
        viewModel.toggleFilter(FilterType.STUDENT_DISCOUNT)
        viewModel.toggleFilter(FilterType.NEAR_TTC)

        viewModel.clearAllFilters()

        val state = viewModel.filterState.value
        assertFalse(state.isUnder15Active)
        assertFalse(state.isStudentDiscountActive)
        assertFalse(state.isNearTTCActive)
    }

    @Test
    fun `clearAllFilters works when no filters are active`() {
        viewModel.clearAllFilters()
        assertFalse(viewModel.filterState.value.hasActiveFilters)
    }
}

/**
 * Unit tests for the pure applyFilters companion function.
 */
class FilterApplyFiltersTest {

    private fun createRestaurant(
        id: String = "1",
        isUnder15: Boolean = false,
        hasStudentDiscount: Boolean = false,
        nearTTC: Boolean = false
    ): Restaurant {
        val priceLevel = if (isUnder15) 1 else 3
        return Restaurant(
            id = id,
            name = "Test Restaurant $id",
            cuisine = "Test",
            priceLevel = priceLevel,
            rating = 4.0f,
            distance = 0.5f,
            imageUrl = null,
            address = "123 Test St",
            location = LatLng(43.7615, -79.3456),
            isSponsored = false,
            hasStudentDiscount = hasStudentDiscount,
            nearTTC = nearTTC,
            averagePrice = if (isUnder15) 12.00f else 25.00f
        )
    }

    private val testRestaurants = listOf(
        createRestaurant(id = "1", isUnder15 = true, hasStudentDiscount = true, nearTTC = true),
        createRestaurant(id = "2", isUnder15 = true, hasStudentDiscount = false, nearTTC = false),
        createRestaurant(id = "3", isUnder15 = false, hasStudentDiscount = true, nearTTC = true),
        createRestaurant(id = "4", isUnder15 = false, hasStudentDiscount = false, nearTTC = false),
        createRestaurant(id = "5", isUnder15 = true, hasStudentDiscount = true, nearTTC = false)
    )

    // ==================== No Filters Active Tests ====================

    @Test
    fun `applyFilters returns all restaurants when no filters active`() {
        val state = FilterState()
        val result = FilterViewModel.applyFilters(testRestaurants, state)
        assertEquals(5, result.size)
    }

    @Test
    fun `applyFilters returns empty list when input is empty`() {
        val state = FilterState(isUnder15Active = true)
        val result = FilterViewModel.applyFilters(emptyList(), state)
        assertTrue(result.isEmpty())
    }

    // ==================== Single Filter Tests ====================

    @Test
    fun `applyFilters filters by Under15 only`() {
        val state = FilterState(isUnder15Active = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(3, result.size)
        assertTrue(result.all { it.isUnder15 })
    }

    @Test
    fun `applyFilters filters by StudentDiscount only`() {
        val state = FilterState(isStudentDiscountActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(3, result.size)
        assertTrue(result.all { it.hasStudentDiscount })
    }

    @Test
    fun `applyFilters filters by NearTTC only`() {
        val state = FilterState(isNearTTCActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(2, result.size)
        assertTrue(result.all { it.nearTTC })
    }

    // ==================== Multiple Filters Tests (AND Logic) ====================

    @Test
    fun `applyFilters uses AND logic for Under15 and StudentDiscount`() {
        val state = FilterState(isUnder15Active = true, isStudentDiscountActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(2, result.size)
        assertTrue(result.all { it.isUnder15 && it.hasStudentDiscount })
    }

    @Test
    fun `applyFilters uses AND logic for Under15 and NearTTC`() {
        val state = FilterState(isUnder15Active = true, isNearTTCActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(1, result.size)
        assertTrue(result.all { it.isUnder15 && it.nearTTC })
    }

    @Test
    fun `applyFilters uses AND logic for StudentDiscount and NearTTC`() {
        val state = FilterState(isStudentDiscountActive = true, isNearTTCActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(2, result.size)
        assertTrue(result.all { it.hasStudentDiscount && it.nearTTC })
    }

    @Test
    fun `applyFilters uses AND logic for all three filters`() {
        val state = FilterState(isUnder15Active = true, isStudentDiscountActive = true, isNearTTCActive = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals(1, result.size)
        val restaurant = result.first()
        assertTrue(restaurant.isUnder15)
        assertTrue(restaurant.hasStudentDiscount)
        assertTrue(restaurant.nearTTC)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `applyFilters returns empty when no restaurants match all filters`() {
        val restaurants = listOf(
            createRestaurant(id = "1", isUnder15 = true, hasStudentDiscount = false, nearTTC = false),
            createRestaurant(id = "2", isUnder15 = false, hasStudentDiscount = true, nearTTC = false),
            createRestaurant(id = "3", isUnder15 = false, hasStudentDiscount = false, nearTTC = true)
        )
        val state = FilterState(isUnder15Active = true, isStudentDiscountActive = true, isNearTTCActive = true)
        val result = FilterViewModel.applyFilters(restaurants, state)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyFilters preserves restaurant order`() {
        val state = FilterState(isUnder15Active = true)
        val result = FilterViewModel.applyFilters(testRestaurants, state)

        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
        assertEquals("5", result[2].id)
    }
}

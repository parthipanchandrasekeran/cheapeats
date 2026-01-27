package com.parthipan.cheapeats.ui.filter

import androidx.lifecycle.ViewModel
import com.parthipan.cheapeats.data.FilterContract
import com.parthipan.cheapeats.data.PriceSource
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the available filter options
 */
enum class FilterType {
    UNDER_15,
    STUDENT_DISCOUNT,
    NEAR_TTC,
    OPEN_NOW
}

/**
 * Price filter strictness mode
 */
enum class PriceFilterMode {
    STRICT,    // Only verified prices <= $15
    FLEXIBLE   // Allows estimated prices up to $17
}

/**
 * UI State for the filter bar - immutable data class following MVVM pattern
 */
data class FilterState(
    val isUnder15Active: Boolean = false,
    val priceFilterMode: PriceFilterMode = PriceFilterMode.STRICT,
    val isStudentDiscountActive: Boolean = false,
    val isNearTTCActive: Boolean = false,
    val isOpenNowActive: Boolean = false,
    val sortBy: SortOption = SortOption.RECOMMENDED
) {
    /**
     * Returns true if any filter is active
     */
    val hasActiveFilters: Boolean
        get() = isUnder15Active || isStudentDiscountActive || isNearTTCActive || isOpenNowActive

    /**
     * Returns the count of active filters
     */
    val activeFilterCount: Int
        get() = listOf(isUnder15Active, isStudentDiscountActive, isNearTTCActive, isOpenNowActive).count { it }

    /**
     * Convert to hard filters for FilterContract enforcement.
     * Hard filters are NEVER violated by AI recommendations.
     */
    fun toHardFilters(): FilterContract.HardFilters {
        return FilterContract.HardFilters(
            mustBeOpen = isOpenNowActive,
            strictUnder15 = isUnder15Active && priceFilterMode == PriceFilterMode.STRICT,
            mustBeNearTTC = isNearTTCActive
        )
    }
}

/**
 * ViewModel for managing restaurant filter state using MVVM architecture.
 *
 * Features:
 * - Immutable state exposed via StateFlow
 * - Pure filtering logic with no side effects
 * - Single source of truth for filter state
 */
class FilterViewModel : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    /**
     * Toggle a specific filter on/off
     */
    fun toggleFilter(filterType: FilterType) {
        _filterState.update { currentState ->
            when (filterType) {
                FilterType.UNDER_15 -> currentState.copy(
                    isUnder15Active = !currentState.isUnder15Active
                )
                FilterType.STUDENT_DISCOUNT -> currentState.copy(
                    isStudentDiscountActive = !currentState.isStudentDiscountActive
                )
                FilterType.NEAR_TTC -> currentState.copy(
                    isNearTTCActive = !currentState.isNearTTCActive
                )
                FilterType.OPEN_NOW -> currentState.copy(
                    isOpenNowActive = !currentState.isOpenNowActive
                )
            }
        }
    }

    /**
     * Set a specific filter state
     */
    fun setFilter(filterType: FilterType, isActive: Boolean) {
        _filterState.update { currentState ->
            when (filterType) {
                FilterType.UNDER_15 -> currentState.copy(isUnder15Active = isActive)
                FilterType.STUDENT_DISCOUNT -> currentState.copy(isStudentDiscountActive = isActive)
                FilterType.NEAR_TTC -> currentState.copy(isNearTTCActive = isActive)
                FilterType.OPEN_NOW -> currentState.copy(isOpenNowActive = isActive)
            }
        }
    }

    /**
     * Set the sort option
     */
    fun setSortOption(sortOption: SortOption) {
        _filterState.update { currentState ->
            currentState.copy(sortBy = sortOption)
        }
    }

    /**
     * Set the price filter mode (Strict/Flexible)
     */
    fun setPriceFilterMode(mode: PriceFilterMode) {
        _filterState.update { currentState ->
            currentState.copy(priceFilterMode = mode)
        }
    }

    /**
     * Clear all active filters
     */
    fun clearAllFilters() {
        _filterState.value = FilterState()
    }

    /**
     * Apply filters to a list of restaurants.
     * Filters are applied with AND logic - restaurant must match ALL active filters.
     *
     * @param restaurants The full list of restaurants to filter
     * @return Filtered list based on current filter state
     */
    fun applyFilters(restaurants: List<Restaurant>): List<Restaurant> {
        return applyFilters(restaurants, _filterState.value)
    }

    companion object {
        /**
         * Pure filtering function that can be used without ViewModel instance.
         * Useful for testing and preview.
         *
         * @param restaurants The list of restaurants to filter
         * @param filterState The current filter state
         * @return Filtered list of restaurants
         */
        fun applyFilters(restaurants: List<Restaurant>, filterState: FilterState): List<Restaurant> {
            // If no filters are active, return all restaurants
            if (!filterState.hasActiveFilters) {
                return restaurants
            }

            return restaurants.filter { restaurant ->
                // Apply AND logic: restaurant must match ALL active filters
                val matchesUnder15 = !filterState.isUnder15Active ||
                    matchesPriceFilter(restaurant, filterState.priceFilterMode)
                val matchesStudentDiscount = !filterState.isStudentDiscountActive || restaurant.hasStudentDiscount
                val matchesNearTTC = !filterState.isNearTTCActive || restaurant.nearTTC
                // For Open Now filter: exclude restaurants known to be closed, include unknown
                val matchesOpenNow = !filterState.isOpenNowActive || restaurant.isOpenNow != false

                matchesUnder15 && matchesStudentDiscount && matchesNearTTC && matchesOpenNow
            }
        }

        /**
         * Check if restaurant matches price filter based on mode.
         * - Strict: verified prices <= $15 only
         * - Flexible: allows estimated prices up to $17
         */
        private fun matchesPriceFilter(restaurant: Restaurant, mode: PriceFilterMode): Boolean {
            return when (mode) {
                PriceFilterMode.STRICT -> {
                    // Strict: must have verified price under $15
                    // Also allow unknown prices (based on priceLevel) to avoid empty results
                    when {
                        restaurant.averagePrice == null -> restaurant.priceLevel <= 1
                        restaurant.priceSource == PriceSource.API_VERIFIED -> restaurant.averagePrice <= 15f
                        restaurant.priceSource == PriceSource.UNKNOWN -> restaurant.averagePrice <= 15f
                        else -> false // Exclude estimated prices in strict mode
                    }
                }
                PriceFilterMode.FLEXIBLE -> {
                    // Flexible: allow up to $17 or low priceLevel
                    restaurant.isFlexiblyUnder15
                }
            }
        }
    }
}

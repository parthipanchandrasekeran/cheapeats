package com.parthipan.cheapeats.ui.filter

import androidx.lifecycle.ViewModel
import com.parthipan.cheapeats.data.Restaurant
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
    NEAR_TTC
}

/**
 * UI State for the filter bar - immutable data class following MVVM pattern
 */
data class FilterState(
    val isUnder15Active: Boolean = false,
    val isStudentDiscountActive: Boolean = false,
    val isNearTTCActive: Boolean = false
) {
    /**
     * Returns true if any filter is active
     */
    val hasActiveFilters: Boolean
        get() = isUnder15Active || isStudentDiscountActive || isNearTTCActive

    /**
     * Returns the count of active filters
     */
    val activeFilterCount: Int
        get() = listOf(isUnder15Active, isStudentDiscountActive, isNearTTCActive).count { it }
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
            }
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
                val matchesUnder15 = !filterState.isUnder15Active || restaurant.isUnder15
                val matchesStudentDiscount = !filterState.isStudentDiscountActive || restaurant.hasStudentDiscount
                val matchesNearTTC = !filterState.isNearTTCActive || restaurant.nearTTC

                matchesUnder15 && matchesStudentDiscount && matchesNearTTC
            }
        }
    }
}

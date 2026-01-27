package com.parthipan.cheapeats.ui.lunchroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.FilterContract
import com.parthipan.cheapeats.data.ReasonGenerator
import com.parthipan.cheapeats.data.RankedRestaurant
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.RestaurantRanker
import com.parthipan.cheapeats.data.TransitHelper
import com.parthipan.cheapeats.data.cache.OfflineManager
import com.parthipan.cheapeats.data.lunchroute.RouteCandidate
import com.parthipan.cheapeats.data.lunchroute.RoutePlan
import com.parthipan.cheapeats.data.lunchroute.RouteStart
import com.parthipan.cheapeats.ui.filter.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Lunch Route feature.
 * Generates actionable lunch plans with primary and backup restaurant options.
 */
class LunchRouteViewModel(
    private val offlineManager: OfflineManager? = null
) : ViewModel() {

    /**
     * State of the lunch route generation.
     */
    sealed class LunchRouteState {
        object Idle : LunchRouteState()
        object Loading : LunchRouteState()
        data class Success(val plan: RoutePlan) : LunchRouteState()
        data class Error(val message: String) : LunchRouteState()
    }

    private val _state = MutableStateFlow<LunchRouteState>(LunchRouteState.Idle)
    val state: StateFlow<LunchRouteState> = _state.asStateFlow()

    private val _startSelection = MutableStateFlow<RouteStart?>(null)
    val startSelection: StateFlow<RouteStart?> = _startSelection.asStateFlow()

    /**
     * Generate a lunch route plan based on available restaurants and filters.
     *
     * @param restaurants Available restaurants to consider
     * @param filterState Current filter state (hard filters will be enforced)
     * @param userLocation User's current location (used if no station selected)
     */
    fun generatePlan(
        restaurants: List<Restaurant>,
        filterState: FilterState,
        userLocation: LatLng?
    ) {
        viewModelScope.launch {
            _state.value = LunchRouteState.Loading

            try {
                // Determine start location
                val startLocation = _startSelection.value
                    ?: userLocation?.let { RouteStart.CurrentLocation(it) }

                if (startLocation == null) {
                    _state.value = LunchRouteState.Error("Location not available")
                    return@launch
                }

                // Get restaurants - use cache if offline
                val isOffline = offlineManager?.isOffline?.value == true
                val sourceRestaurants = if (isOffline && offlineManager != null) {
                    offlineManager.getCachedResults(startLocation.location, filterState)
                } else {
                    restaurants
                }

                if (sourceRestaurants.isEmpty()) {
                    _state.value = LunchRouteState.Error(
                        if (isOffline) "No cached restaurants available"
                        else "No restaurants available"
                    )
                    return@launch
                }

                // Apply hard filters
                val hardFilters = filterState.toHardFilters()
                val filtered = FilterContract.applyHardFilters(sourceRestaurants, hardFilters)

                if (filtered.isEmpty()) {
                    _state.value = LunchRouteState.Error("No restaurants match your filters")
                    return@launch
                }

                // Rank restaurants (uses lunch weights automatically during 11am-2pm)
                val ranked = RestaurantRanker.rank(
                    restaurants = filtered,
                    excludeClosed = true,
                    requireUnder15 = hardFilters.strictUnder15
                )

                if (ranked.isEmpty()) {
                    _state.value = LunchRouteState.Error("No open restaurants found")
                    return@launch
                }

                // Build candidates from top 2 results
                val primary = buildCandidate(ranked[0], startLocation.location, filterState)
                val backup = ranked.getOrNull(1)?.let {
                    buildCandidate(it, startLocation.location, filterState)
                }

                val plan = RoutePlan(
                    primary = primary,
                    backup = backup,
                    startLocation = startLocation,
                    isFromCache = isOffline
                )

                _state.value = LunchRouteState.Success(plan)
            } catch (e: Exception) {
                _state.value = LunchRouteState.Error("Failed to generate plan: ${e.message}")
            }
        }
    }

    /**
     * Select the starting location for the route.
     */
    fun selectStartLocation(start: RouteStart) {
        _startSelection.value = start
    }

    /**
     * Clear the current plan and reset state.
     */
    fun clearPlan() {
        _state.value = LunchRouteState.Idle
        _startSelection.value = null
    }

    /**
     * Build a RouteCandidate from a RankedRestaurant.
     */
    private fun buildCandidate(
        ranked: RankedRestaurant,
        startLocation: LatLng,
        filterState: FilterState
    ): RouteCandidate {
        val restaurant = ranked.restaurant
        val reasons = ReasonGenerator.generateReasons(restaurant, filterState)

        // Calculate ETA from start location
        val distanceMeters = TransitHelper.calculateDistanceMeters(startLocation, restaurant.location)
        val etaMinutes = TransitHelper.walkingTimeMinutes(distanceMeters)

        return RouteCandidate(
            restaurant = restaurant,
            reasons = reasons,
            etaMinutes = etaMinutes,
            walkFromStation = restaurant.ttcWalkMinutes,
            nearestStation = restaurant.nearestStation,
            score = ranked.score,
            explanation = ranked.explanation
        )
    }

    companion object {
        /**
         * Check if current time is during lunch hours (11 AM - 2 PM).
         */
        fun isLunchTime(): Boolean {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return hour in 11..13
        }
    }
}

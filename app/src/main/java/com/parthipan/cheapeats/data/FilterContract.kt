package com.parthipan.cheapeats.data

/**
 * Contract for filter enforcement.
 * Hard filters are NEVER violated - restaurants that don't match are excluded entirely.
 * Soft preferences influence ranking but don't exclude.
 */
object FilterContract {

    /**
     * HARD FILTERS - These are NEVER violated by AI.
     * If a restaurant doesn't match, it's excluded entirely.
     */
    data class HardFilters(
        val mustBeOpen: Boolean = false,
        val strictUnder15: Boolean = false,
        val mustBeNearTTC: Boolean = false,
        val maxWalkMinutes: Int? = null
    )

    /**
     * SOFT PREFERENCES - These influence ranking but don't exclude.
     */
    data class SoftPreferences(
        val preferHighRating: Boolean = true,
        val preferVerifiedPrices: Boolean = true,
        val preferQuickService: Boolean = false
    )

    /**
     * Validates a restaurant against hard filters.
     * Returns null if restaurant should be excluded, or the restaurant if valid.
     */
    fun validateHardFilters(
        restaurant: Restaurant,
        filters: HardFilters
    ): Restaurant? {
        // Open Now - HARD FILTER
        if (filters.mustBeOpen && restaurant.isOpenNow != true) {
            return null  // Unknown or closed = excluded
        }

        // Strict Under $15 - HARD FILTER
        if (filters.strictUnder15 && !restaurant.isVerifiedUnder15) {
            return null  // Only verified prices pass
        }

        // Near TTC - HARD FILTER
        if (filters.mustBeNearTTC && !restaurant.nearTTC) {
            return null
        }

        // Max walk time - HARD FILTER
        if (filters.maxWalkMinutes != null) {
            val walkTime = restaurant.ttcWalkMinutes ?: Int.MAX_VALUE
            if (walkTime > filters.maxWalkMinutes) {
                return null
            }
        }

        return restaurant
    }

    /**
     * Filter a list of restaurants using hard filters.
     * This is the primary entry point for filter enforcement.
     */
    fun applyHardFilters(
        restaurants: List<Restaurant>,
        filters: HardFilters
    ): List<Restaurant> {
        return restaurants.mapNotNull { validateHardFilters(it, filters) }
    }
}

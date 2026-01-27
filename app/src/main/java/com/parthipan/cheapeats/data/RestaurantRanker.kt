package com.parthipan.cheapeats.data

/**
 * Represents a restaurant with its calculated ranking score and descriptive info.
 */
data class RankedRestaurant(
    val restaurant: Restaurant,
    val score: Float,
    val explanation: String,  // Casual Toronto-local description
    val trustLabel: String    // Data freshness indicator
)

/**
 * Ranking and filtering logic for restaurants.
 * Implements multi-factor ranking based on value, TTC proximity, and rating.
 */
object RestaurantRanker {

    // Scoring weights
    private const val VALUE_WEIGHT = 0.4f
    private const val TTC_WEIGHT = 0.3f
    private const val RATING_WEIGHT = 0.3f

    // Walking speed in meters per minute (average walking pace)
    private const val WALKING_SPEED_M_PER_MIN = 80f

    /**
     * Rank restaurants using multi-factor scoring.
     * Filters out closed restaurants and those without affordable options.
     *
     * @param restaurants List of restaurants to rank
     * @param excludeClosed Whether to exclude restaurants that are known to be closed
     * @param requireUnder15 Whether to exclude restaurants without items under $15
     * @return Ranked list of restaurants with scores and explanations
     */
    fun rank(
        restaurants: List<Restaurant>,
        excludeClosed: Boolean = true,
        requireUnder15: Boolean = false
    ): List<RankedRestaurant> {
        return restaurants
            .filter { !excludeClosed || it.isOpenNow != false } // Exclude closed, keep unknown
            .filter { !requireUnder15 || it.isUnder15 || it.averagePrice == null } // Keep if under $15 or unknown
            .map { calculateScore(it) }
            .sortedByDescending { it.score }
    }

    /**
     * Calculate the composite score for a restaurant.
     * Score is based on value (price), TTC proximity, and rating.
     */
    private fun calculateScore(restaurant: Restaurant): RankedRestaurant {
        val valueScore = calculateValueScore(restaurant)
        val ttcScore = calculateTTCScore(restaurant)
        val ratingScore = restaurant.rating / 5f

        val totalScore = (valueScore * VALUE_WEIGHT) +
                         (ttcScore * TTC_WEIGHT) +
                         (ratingScore * RATING_WEIGHT)

        return RankedRestaurant(
            restaurant = restaurant,
            score = totalScore,
            explanation = generateExplanation(restaurant),
            trustLabel = getTrustLabel(restaurant)
        )
    }

    /**
     * Calculate value score based on price.
     * Lower prices = higher score.
     */
    private fun calculateValueScore(restaurant: Restaurant): Float {
        return if (restaurant.averagePrice != null) {
            // Score decreases as price increases, max score at $0, 0 score at $15+
            ((15f - restaurant.averagePrice) / 15f).coerceIn(0f, 1f)
        } else {
            0.5f // Default for unknown price
        }
    }

    /**
     * Calculate TTC proximity score based on walking time.
     * Closer to station = higher score.
     */
    private fun calculateTTCScore(restaurant: Restaurant): Float {
        return restaurant.ttcWalkMinutes?.let { walkMinutes ->
            // Score decreases as walk time increases, 0 score at 10+ minutes
            (1f - (walkMinutes / 10f)).coerceIn(0f, 1f)
        } ?: 0.5f // Default for unknown walk time
    }

    /**
     * Generate a casual, Toronto-local explanation (max ~15 words).
     */
    private fun generateExplanation(restaurant: Restaurant): String {
        val parts = mutableListOf<String>()

        // Price commentary
        when {
            restaurant.averagePrice != null && restaurant.averagePrice < 12f ->
                parts.add("Stupid cheap")
            restaurant.isUnder15 ->
                parts.add("Budget-friendly")
        }

        // TTC commentary
        restaurant.nearestStation?.let { station ->
            if (restaurant.ttcWalkMinutes != null && restaurant.ttcWalkMinutes <= 5) {
                parts.add("steps from $station")
            } else if (restaurant.nearTTC) {
                parts.add("near $station")
            }
        }

        // Rating commentary
        when {
            restaurant.rating >= 4.5f -> parts.add("locals love it")
            restaurant.rating >= 4.0f -> parts.add("solid ${restaurant.rating} stars")
            restaurant.rating >= 3.5f -> parts.add("decent spot")
        }

        // Open status
        if (restaurant.isOpenNow == true) {
            parts.add("open now")
        }

        // Fallback if nothing else
        if (parts.isEmpty()) {
            parts.add(restaurant.cuisine.lowercase())
        }

        return parts.joinToString(", ").take(60)
    }

    /**
     * Generate a trust label based on data freshness.
     */
    private fun getTrustLabel(restaurant: Restaurant): String {
        return when (restaurant.dataFreshness) {
            DataFreshness.LIVE -> "Live data"
            DataFreshness.RECENT -> "Updated recently"
            DataFreshness.CACHED -> {
                restaurant.lastVerified?.let { timestamp ->
                    val hoursAgo = (System.currentTimeMillis() - timestamp) / 3600000
                    when {
                        hoursAgo < 1 -> "Updated recently"
                        hoursAgo < 24 -> "Cached ${hoursAgo}h ago"
                        else -> "Cached ${hoursAgo / 24}d ago"
                    }
                } ?: "Cached"
            }
            DataFreshness.UNKNOWN -> "Unverified"
        }
    }

    /**
     * Filter restaurants by open status only.
     */
    fun filterOpen(restaurants: List<Restaurant>): List<Restaurant> {
        return restaurants.filter { it.isOpenNow != false }
    }

    /**
     * Filter restaurants to only those with items under $15.
     * Restaurants with unknown prices are included (fallback behavior).
     */
    fun filterUnder15(restaurants: List<Restaurant>): List<Restaurant> {
        return restaurants.filter { it.isUnder15 || it.averagePrice == null }
    }

    /**
     * Sort restaurants by a specific criterion.
     */
    fun sortBy(restaurants: List<Restaurant>, option: SortOption): List<Restaurant> {
        return when (option) {
            SortOption.RECOMMENDED -> rank(restaurants).map { it.restaurant }
            SortOption.PRICE_LOW -> restaurants.sortedBy { it.averagePrice ?: Float.MAX_VALUE }
            SortOption.RATING_HIGH -> restaurants.sortedByDescending { it.rating }
            SortOption.NEAREST_TTC -> restaurants.sortedBy { it.ttcWalkMinutes ?: Int.MAX_VALUE }
        }
    }

    /**
     * Calculate walking time in minutes from distance in meters.
     */
    fun walkingTimeMinutes(distanceMeters: Float): Int {
        return (distanceMeters / WALKING_SPEED_M_PER_MIN).toInt()
    }
}

/**
 * Sort options for restaurant list.
 */
enum class SortOption {
    RECOMMENDED,   // Multi-factor score
    PRICE_LOW,     // Lowest price first
    RATING_HIGH,   // Highest rating first
    NEAREST_TTC    // Closest to TTC first
}

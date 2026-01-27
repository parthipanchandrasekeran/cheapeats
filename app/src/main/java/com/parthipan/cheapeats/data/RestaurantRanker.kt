package com.parthipan.cheapeats.data

import com.parthipan.cheapeats.ui.filter.FilterState
import java.util.Calendar

/**
 * Ranking and filtering logic for restaurants.
 * Implements multi-factor ranking based on value, TTC proximity, and rating.
 * Time-aware: During lunch hours (11 AM - 2 PM), prioritizes speed and TTC proximity.
 */
object RestaurantRanker {

    // Default scoring weights
    private const val VALUE_WEIGHT = 0.4f
    private const val TTC_WEIGHT = 0.3f
    private const val RATING_WEIGHT = 0.3f

    // Lunch hour weights (11 AM - 2 PM): prioritize TTC proximity over rating
    private const val LUNCH_VALUE_WEIGHT = 0.35f
    private const val LUNCH_TTC_WEIGHT = 0.45f
    private const val LUNCH_RATING_WEIGHT = 0.20f

    // Favorite boost (applied as multiplier to score)
    private const val FAVORITE_BOOST = 1.15f

    // Walking speed in meters per minute (average walking pace)
    private const val WALKING_SPEED_M_PER_MIN = 80f

    /**
     * Check if current time is during lunch hours (11 AM - 2 PM).
     */
    private fun isLunchHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 11..13 // 11:00 AM to 1:59 PM
    }

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
        val ranked = restaurants
            .filter { !excludeClosed || it.isOpenNow != false } // Exclude closed, keep unknown
            .filter { !requireUnder15 || it.isUnder15 || it.averagePrice == null } // Keep if under $15 or unknown
            .map { calculateScore(it) }
            .sortedByDescending { it.score }
            .toMutableList()

        // Never rank UNKNOWN data freshness as #1
        // If #1 has unknown freshness and there's a verified option, swap them
        if (ranked.size > 1 &&
            ranked[0].restaurant.dataFreshness == DataFreshness.UNKNOWN) {
            val firstVerified = ranked.indexOfFirst {
                it.restaurant.dataFreshness != DataFreshness.UNKNOWN
            }
            if (firstVerified > 0) {
                val temp = ranked[0]
                ranked[0] = ranked[firstVerified]
                ranked[firstVerified] = temp
            }
        }

        return ranked
    }

    /**
     * Calculate the composite score for a restaurant.
     * Score is based on value (price), TTC proximity, and rating.
     * During lunch hours: TTC proximity weighted higher for quick access.
     * Favorites get a boost if they're open and reasonably priced.
     */
    private fun calculateScore(restaurant: Restaurant): RankedRestaurant {
        val valueScore = calculateValueScore(restaurant)
        val ttcScore = calculateTTCScore(restaurant)
        val ratingScore = restaurant.rating / 5f

        // Use lunch-optimized weights during 11 AM - 2 PM
        val (vWeight, tWeight, rWeight) = if (isLunchHours()) {
            Triple(LUNCH_VALUE_WEIGHT, LUNCH_TTC_WEIGHT, LUNCH_RATING_WEIGHT)
        } else {
            Triple(VALUE_WEIGHT, TTC_WEIGHT, RATING_WEIGHT)
        }

        var totalScore = (valueScore * vWeight) +
                         (ttcScore * tWeight) +
                         (ratingScore * rWeight)

        // Apply favorite boost only if:
        // - Restaurant is a favorite
        // - Not known to be closed
        // - Not overpriced (under $15 or unknown)
        if (restaurant.isFavorite &&
            restaurant.isOpenNow != false &&
            (restaurant.isUnder15 || restaurant.averagePrice == null)) {
            totalScore *= FAVORITE_BOOST
        }

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

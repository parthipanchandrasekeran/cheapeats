package com.parthipan.cheapeats.data

import com.parthipan.cheapeats.ui.filter.FilterState

/**
 * Generates recommendation reasons for restaurants.
 * Used to show users why each restaurant was picked.
 */
object ReasonGenerator {

    private const val MAX_REASONS = 4

    /**
     * Generate reasons why this restaurant was recommended.
     */
    fun generateReasons(
        restaurant: Restaurant,
        filterState: FilterState,
        searchQuery: String? = null
    ): List<RecommendationReason> {
        val reasons = mutableListOf<RecommendationReason>()

        // Open status
        if (restaurant.isOpenNow == true) {
            reasons.add(RecommendationReason.OPEN_NOW)
        }

        // Price verification
        when {
            restaurant.isVerifiedUnder15 ->
                reasons.add(RecommendationReason.VERIFIED_UNDER_15)
            restaurant.isFlexiblyUnder15 && restaurant.priceSource == PriceSource.ESTIMATED ->
                reasons.add(RecommendationReason.ESTIMATED_UNDER_15)
        }

        // TTC proximity
        if (restaurant.nearTTC && restaurant.ttcWalkMinutes?.let { it <= 5 } == true) {
            reasons.add(RecommendationReason.NEAR_TTC)
        }

        // Rating
        if (restaurant.rating >= 4.3f) {
            reasons.add(RecommendationReason.HIGH_RATING)
        }

        // Search match
        if (!searchQuery.isNullOrBlank() &&
            (restaurant.name.contains(searchQuery, ignoreCase = true) ||
             restaurant.cuisine.contains(searchQuery, ignoreCase = true))) {
            reasons.add(RecommendationReason.QUERY_MATCH)
        }

        // Student discount
        if (restaurant.hasStudentDiscount) {
            reasons.add(RecommendationReason.STUDENT_DISCOUNT)
        }

        return reasons.take(MAX_REASONS)
    }

    /**
     * Generate a human-readable explanation from the reasons.
     */
    fun generateExplanation(reasons: List<RecommendationReason>): String {
        if (reasons.isEmpty()) return "Nearby option"

        val parts = reasons.take(2).map { reason ->
            when (reason) {
                RecommendationReason.OPEN_NOW -> "open now"
                RecommendationReason.VERIFIED_UNDER_15 -> "verified cheap"
                RecommendationReason.ESTIMATED_UNDER_15 -> "likely affordable"
                RecommendationReason.NEAR_TTC -> "steps from transit"
                RecommendationReason.HIGH_RATING -> "locals love it"
                RecommendationReason.QUERY_MATCH -> "matches your search"
                RecommendationReason.STUDENT_DISCOUNT -> "student discount"
                RecommendationReason.QUICK_SERVICE -> "quick service"
                RecommendationReason.LUNCH_SPECIAL -> "lunch special"
            }
        }
        return parts.joinToString(", ").replaceFirstChar { it.uppercase() }
    }

    /**
     * Create a RankedRestaurant with reasons and explanation.
     */
    fun rankWithReasons(
        restaurant: Restaurant,
        score: Float,
        filterState: FilterState,
        searchQuery: String? = null
    ): RankedRestaurant {
        val reasons = generateReasons(restaurant, filterState, searchQuery)
        val explanation = generateExplanation(reasons)
        return RankedRestaurant(
            restaurant = restaurant,
            score = score,
            reasons = reasons,
            explanation = explanation
        )
    }
}

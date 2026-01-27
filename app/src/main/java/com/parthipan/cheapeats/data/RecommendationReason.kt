package com.parthipan.cheapeats.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Reasons why a restaurant was recommended to the user.
 * Used for "Why this pick?" explanation chips.
 */
enum class RecommendationReason(
    val label: String,
    val icon: ImageVector
) {
    OPEN_NOW("Open now", Icons.Default.Check),
    VERIFIED_UNDER_15("Under $15 verified", Icons.Default.CheckCircle),
    ESTIMATED_UNDER_15("~Under $15", Icons.Default.Info),
    NEAR_TTC("Near TTC", Icons.Default.LocationOn),
    HIGH_RATING("Highly rated", Icons.Default.Star),
    QUERY_MATCH("Matches search", Icons.Default.Search),
    STUDENT_DISCOUNT("Student deal", Icons.Default.Person),
    QUICK_SERVICE("Fast service", Icons.Default.Check),
    LUNCH_SPECIAL("Lunch special", Icons.Default.Info),
    FASTEST_OPTION("Fastest lunch", Icons.Default.Check)
}

/**
 * A restaurant with its recommendation score and reasons.
 */
data class RankedRestaurant(
    val restaurant: Restaurant,
    val score: Float,
    val reasons: List<RecommendationReason> = emptyList(),
    val explanation: String,
    val trustLabel: String = ""
)

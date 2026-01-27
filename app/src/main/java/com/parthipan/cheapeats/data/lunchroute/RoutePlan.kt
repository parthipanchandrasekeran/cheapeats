package com.parthipan.cheapeats.data.lunchroute

import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.RecommendationReason
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.SubwayStation

/**
 * A complete lunch route plan with primary and backup options.
 */
data class RoutePlan(
    val primary: RouteCandidate,
    val backup: RouteCandidate?,
    val startLocation: RouteStart,
    val generatedAt: Long = System.currentTimeMillis(),
    val isFromCache: Boolean = false
)

/**
 * Starting point for the lunch route.
 */
sealed class RouteStart {
    data class CurrentLocation(val latLng: LatLng) : RouteStart()
    data class TTCStation(val station: SubwayStation) : RouteStart()

    val displayName: String
        get() = when (this) {
            is CurrentLocation -> "Current Location"
            is TTCStation -> "${station.name} Station"
        }

    val location: LatLng
        get() = when (this) {
            is CurrentLocation -> latLng
            is TTCStation -> station.location
        }
}

/**
 * A candidate restaurant in the route plan.
 */
data class RouteCandidate(
    val restaurant: Restaurant,
    val reasons: List<RecommendationReason>,
    val etaMinutes: Int,
    val walkFromStation: Int?,
    val nearestStation: String?,
    val score: Float,
    val explanation: String
) {
    /**
     * Human-readable ETA string.
     */
    val etaDisplay: String
        get() = when {
            etaMinutes <= 1 -> "1 min walk"
            etaMinutes < 60 -> "$etaMinutes min walk"
            else -> {
                val hours = etaMinutes / 60
                val mins = etaMinutes % 60
                if (mins == 0) "${hours}h walk" else "${hours}h ${mins}m walk"
            }
        }

    /**
     * Whether this is a fast option (under 5 minutes).
     */
    val isFastOption: Boolean
        get() = etaMinutes <= 5
}

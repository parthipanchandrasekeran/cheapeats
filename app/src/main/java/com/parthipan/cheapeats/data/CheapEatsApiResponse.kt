package com.parthipan.cheapeats.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * API response format for CheapEats restaurant data.
 * Suitable for JSON serialization.
 */
data class CheapEatsApiResponse(
    val generated_at: String,
    val data_source: String,
    val fallback_used: Boolean,
    val result_count: Int,
    val results: List<RestaurantResult>
) {
    companion object {
        /**
         * Create an API response from ranked restaurants.
         */
        fun fromRankedRestaurants(
            rankedRestaurants: List<RankedRestaurant>,
            fallbackUsed: Boolean = false,
            dataSource: String = "google_places"
        ): CheapEatsApiResponse {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            return CheapEatsApiResponse(
                generated_at = dateFormat.format(Date()),
                data_source = if (fallbackUsed) "cached" else dataSource,
                fallback_used = fallbackUsed,
                result_count = rankedRestaurants.size,
                results = rankedRestaurants.map { RestaurantResult.fromRankedRestaurant(it) }
            )
        }
    }
}

/**
 * Individual restaurant result in API response.
 */
data class RestaurantResult(
    val restaurant_id: String,
    val restaurant_name: String,
    val cuisine: String,
    val rating: Float,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val ttc_walk_minutes: Int?,
    val nearest_station: String?,
    val open_now: Boolean?,
    val opening_hours: String?,
    val best_item: String?,        // Future: specific menu item
    val price_cad: Float?,
    val price_unknown: Boolean,
    val explanation: String,
    val trust_label: String,
    val score: Float,
    val website_url: String?,
    val google_maps_url: String?
) {
    companion object {
        /**
         * Create a RestaurantResult from a RankedRestaurant.
         */
        fun fromRankedRestaurant(ranked: RankedRestaurant): RestaurantResult {
            val r = ranked.restaurant
            return RestaurantResult(
                restaurant_id = r.id,
                restaurant_name = r.name,
                cuisine = r.cuisine,
                rating = r.rating,
                address = r.address,
                latitude = r.latitude,
                longitude = r.longitude,
                ttc_walk_minutes = r.ttcWalkMinutes,
                nearest_station = r.nearestStation,
                open_now = r.isOpenNow,
                opening_hours = r.openingHours,
                best_item = null, // Future implementation
                price_cad = r.averagePrice,
                price_unknown = r.averagePrice == null,
                explanation = ranked.explanation,
                trust_label = ranked.trustLabel,
                score = ranked.score,
                website_url = r.websiteUrl,
                google_maps_url = r.googleMapsUrl
            )
        }
    }
}

/**
 * Error response for API failures.
 */
data class CheapEatsErrorResponse(
    val error: String,
    val error_code: String,
    val fallback_available: Boolean,
    val timestamp: String
) {
    companion object {
        fun create(error: String, errorCode: String, fallbackAvailable: Boolean): CheapEatsErrorResponse {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            return CheapEatsErrorResponse(
                error = error,
                error_code = errorCode,
                fallback_available = fallbackAvailable,
                timestamp = dateFormat.format(Date())
            )
        }
    }
}

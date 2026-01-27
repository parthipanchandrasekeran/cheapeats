package com.parthipan.cheapeats.data.map

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.parthipan.cheapeats.data.Restaurant

/**
 * A hint showing an area with multiple cheap restaurants.
 */
data class CheapAreaHint(
    val center: LatLng,
    val radius: Float,
    val restaurantCount: Int,
    val avgPrice: Float,
    val label: String
)

/**
 * Calculates areas with high density of cheap restaurants.
 * Uses lightweight grid-based clustering (not k-means).
 */
object CheapAreaCalculator {

    private const val CLUSTER_RADIUS_METERS = 300.0
    private const val MIN_CLUSTER_SIZE = 3
    private const val GRID_SIZE = 0.003  // ~300m at Toronto latitude

    /**
     * Find areas with clusters of cheap restaurants.
     */
    fun calculateCheapAreas(
        restaurants: List<Restaurant>,
        bounds: LatLngBounds
    ): List<CheapAreaHint> {
        // Filter to cheap restaurants in view
        val cheapRestaurants = restaurants.filter {
            it.isFlexiblyUnder15 && bounds.contains(it.location)
        }

        if (cheapRestaurants.size < MIN_CLUSTER_SIZE) return emptyList()

        // Simple grid-based clustering
        val clusters = cheapRestaurants
            .groupBy { r ->
                val latBucket = (r.location.latitude / GRID_SIZE).toInt()
                val lngBucket = (r.location.longitude / GRID_SIZE).toInt()
                latBucket to lngBucket
            }
            .filter { it.value.size >= MIN_CLUSTER_SIZE }

        return clusters.map { (_, clusterRestaurants) ->
            val avgLat = clusterRestaurants.map { it.location.latitude }.average()
            val avgLng = clusterRestaurants.map { it.location.longitude }.average()
            val prices = clusterRestaurants.mapNotNull { it.averagePrice }
            val avgPrice = if (prices.isNotEmpty()) prices.average() else 12.0

            CheapAreaHint(
                center = LatLng(avgLat, avgLng),
                radius = CLUSTER_RADIUS_METERS.toFloat(),
                restaurantCount = clusterRestaurants.size,
                avgPrice = avgPrice.toFloat(),
                label = "${clusterRestaurants.size} spots ~$${avgPrice.toInt()}"
            )
        }
    }
}

package com.parthipan.cheapeats.data.cache

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.DataFreshness
import com.parthipan.cheapeats.data.PriceSource
import com.parthipan.cheapeats.data.Restaurant

/**
 * Cached restaurant for offline access.
 */
@Entity(
    tableName = "cached_restaurants",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["averagePrice"]),
        Index(value = ["lastAccessedAt"]),
        Index(value = ["rating"]),
        Index(value = ["nearTTC"])
    ]
)
data class CachedRestaurant(
    @PrimaryKey
    val id: String,

    // Core data
    val name: String,
    val cuisine: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val priceLevel: Int,
    val rating: Float,
    val nearTTC: Boolean,
    val hasStudentDiscount: Boolean,

    // Price data
    val averagePrice: Float?,
    val priceSource: String,

    // Open hours
    val isOpenNow: Boolean?,
    val openingHoursJson: String?,

    // Images
    val imageUrl: String?,
    val thumbnailPath: String?,

    // Cache metadata
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val dataFreshness: String = DataFreshness.CACHED.name,

    // Location context
    val cachedNearLat: Double?,
    val cachedNearLng: Double?
)

/**
 * Convert Restaurant to CachedRestaurant.
 */
fun Restaurant.toCached(userLocation: LatLng?): CachedRestaurant {
    return CachedRestaurant(
        id = id,
        name = name,
        cuisine = cuisine,
        address = address,
        latitude = location.latitude,
        longitude = location.longitude,
        priceLevel = priceLevel,
        rating = rating,
        nearTTC = nearTTC,
        hasStudentDiscount = hasStudentDiscount,
        averagePrice = averagePrice,
        priceSource = priceSource.name,
        isOpenNow = isOpenNow,
        openingHoursJson = null,
        imageUrl = imageUrl,
        thumbnailPath = null,
        cachedNearLat = userLocation?.latitude,
        cachedNearLng = userLocation?.longitude
    )
}

/**
 * Convert CachedRestaurant back to Restaurant.
 */
fun CachedRestaurant.toRestaurant(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        cuisine = cuisine,
        priceLevel = priceLevel,
        rating = rating,
        distance = 0f,  // Distance will be recalculated from current location
        imageUrl = thumbnailPath ?: imageUrl,
        address = address,
        location = LatLng(latitude, longitude),
        isSponsored = false,
        hasStudentDiscount = hasStudentDiscount,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        priceSource = try {
            PriceSource.valueOf(priceSource)
        } catch (e: Exception) {
            PriceSource.UNKNOWN
        },
        isOpenNow = isOpenNow,
        dataFreshness = DataFreshness.CACHED
    )
}

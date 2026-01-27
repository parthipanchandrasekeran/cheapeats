package com.parthipan.cheapeats.data

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class PlacesService(context: Context, private val apiKey: String) {

    companion object {
        private const val TAG = "PlacesService"

        // Pre-computed cuisine type map for O(1) lookup
        private val cuisineTypeMap = mapOf(
            "chinese_restaurant" to "Chinese",
            "italian_restaurant" to "Italian",
            "japanese_restaurant" to "Japanese",
            "mexican_restaurant" to "Mexican",
            "indian_restaurant" to "Indian",
            "thai_restaurant" to "Thai",
            "vietnamese_restaurant" to "Vietnamese",
            "korean_restaurant" to "Korean",
            "american_restaurant" to "American",
            "mediterranean_restaurant" to "Mediterranean",
            "pizza" to "Pizza",
            "seafood" to "Seafood",
            "steak_house" to "Steakhouse",
            "sushi" to "Sushi",
            "cafe" to "Cafe",
            "fast_food" to "Fast Food",
            "bakery" to "Bakery",
            "bar" to "Bar & Grill",
            "meal_takeaway" to "Takeaway",
            "meal_delivery" to "Delivery"
        )
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // Reduced from 30s
        .readTimeout(15, TimeUnit.SECONDS)     // Reduced from 30s
        .retryOnConnectionFailure(true)
        .build()

    private val gson = Gson()

    suspend fun searchNearbyRestaurants(
        location: LatLng,
        radiusMeters: Int = 1500
    ): List<Restaurant> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching for restaurants near: ${location.latitude}, ${location.longitude}")

            // Use the new Places API (New) format
            val url = "https://places.googleapis.com/v1/places:searchNearby"

            val requestBody = """
                {
                    "includedTypes": ["restaurant"],
                    "maxResultCount": 20,
                    "locationRestriction": {
                        "circle": {
                            "center": {
                                "latitude": ${location.latitude},
                                "longitude": ${location.longitude}
                            },
                            "radius": $radiusMeters
                        }
                    }
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("X-Goog-Api-Key", apiKey)
                .addHeader("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.priceLevel,places.types,places.photos,places.websiteUri,places.googleMapsUri,places.regularOpeningHours,places.currentOpeningHours,places.businessStatus")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: run {
                Log.e(TAG, "Response body is null")
                return@withContext emptyList()
            }

            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e(TAG, "Request failed: ${response.code} - $responseBody")
                return@withContext emptyList()
            }

            val placesResponse = gson.fromJson(responseBody, NewPlacesApiResponse::class.java)
            Log.d(TAG, "Results count: ${placesResponse.places?.size ?: 0}")

            val currentTime = System.currentTimeMillis()
            val restaurantList = placesResponse.places?.map { place ->
                Log.d(TAG, "Found: ${place.displayName?.text} - Price: ${place.priceLevel} - Rating: ${place.rating}")
                val placeLocation = place.location
                val distance = if (placeLocation != null) {
                    calculateDistance(
                        location.latitude, location.longitude,
                        placeLocation.latitude, placeLocation.longitude
                    )
                } else 0f

                val restaurantLatLng = LatLng(
                    placeLocation?.latitude ?: location.latitude,
                    placeLocation?.longitude ?: location.longitude
                )

                // Calculate TTC walking time
                val nearestStationPair = TransitHelper.findNearestStation(restaurantLatLng)
                val ttcWalkMinutes = nearestStationPair?.let { (_, distanceMeters) ->
                    RestaurantRanker.walkingTimeMinutes(distanceMeters)
                }
                val nearestStationName = nearestStationPair?.first?.name

                // Parse opening hours
                val isOpenNow = place.currentOpeningHours?.openNow
                    ?: place.regularOpeningHours?.openNow
                val openingHoursText = place.currentOpeningHours?.weekdayDescriptions?.firstOrNull()
                    ?: place.regularOpeningHours?.weekdayDescriptions?.firstOrNull()

                // Determine data freshness based on whether we have live opening hours
                val dataFreshness = when {
                    place.currentOpeningHours?.openNow != null -> DataFreshness.LIVE
                    place.regularOpeningHours?.openNow != null -> DataFreshness.RECENT
                    else -> DataFreshness.UNKNOWN
                }

                // Determine price source - ESTIMATED since we calculate from priceLevel
                val priceSource = if (place.priceLevel != null) {
                    PriceSource.ESTIMATED
                } else {
                    PriceSource.UNKNOWN
                }

                Restaurant(
                    id = place.id ?: "place_${place.displayName?.text.hashCode()}",
                    name = place.displayName?.text ?: "Unknown Restaurant",
                    cuisine = getCuisineType(place.types ?: emptyList()),
                    priceLevel = parsePriceLevel(place.priceLevel),
                    rating = place.rating?.toFloat() ?: 0f,
                    distance = distance,
                    imageUrl = place.photos?.firstOrNull()?.let { photo ->
                        "https://places.googleapis.com/v1/${photo.name}/media?maxWidthPx=400&key=$apiKey"
                    },
                    address = place.formattedAddress ?: "",
                    location = restaurantLatLng,
                    isSponsored = false,
                    hasStudentDiscount = false, // Would need external data source
                    nearTTC = TransitHelper.isTransitAccessible(restaurantLatLng),
                    averagePrice = estimateAveragePrice(parsePriceLevel(place.priceLevel)),
                    priceSource = priceSource,
                    websiteUrl = place.websiteUri,
                    googleMapsUrl = place.googleMapsUri,
                    isOpenNow = isOpenNow,
                    openingHours = openingHoursText,
                    ttcWalkMinutes = ttcWalkMinutes,
                    nearestStation = nearestStationName,
                    dataFreshness = dataFreshness,
                    lastVerified = currentTime
                )
            } ?: emptyList()

            Log.d(TAG, "Returning ${restaurantList.size} restaurants")
            restaurantList
        } catch (e: Exception) {
            Log.e(TAG, "Exception in searchNearbyRestaurants", e)
            emptyList()
        }
    }

    private fun parsePriceLevel(priceLevel: String?): Int {
        return when (priceLevel) {
            "PRICE_LEVEL_FREE" -> 0
            "PRICE_LEVEL_INEXPENSIVE" -> 1
            "PRICE_LEVEL_MODERATE" -> 2
            "PRICE_LEVEL_EXPENSIVE" -> 3
            "PRICE_LEVEL_VERY_EXPENSIVE" -> 4
            else -> 1
        }
    }

    /**
     * Estimates average meal price based on Google's price level
     */
    private fun estimateAveragePrice(priceLevel: Int): Float {
        return when (priceLevel) {
            0 -> 0f      // Free
            1 -> 12f     // $ - Under $15
            2 -> 22f     // $$ - $15-30
            3 -> 40f     // $$$ - $30-50
            4 -> 65f     // $$$$ - $50+
            else -> 15f
        }
    }

    private fun getCuisineType(types: List<String>): String {
        // O(n) where n = types.size, instead of O(n * m) where m = cuisineTypes
        for (type in types) {
            cuisineTypeMap[type]?.let { return it }
        }
        return "Restaurant"
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        // Convert meters to miles
        return results[0] / 1609.34f
    }
}

// New Places API (v1) Response data classes
data class NewPlacesApiResponse(
    val places: List<NewPlaceResult>?
)

data class NewPlaceResult(
    val id: String?,
    val displayName: DisplayName?,
    val formattedAddress: String?,
    val location: LocationResult?,
    val rating: Double?,
    val priceLevel: String?,
    val types: List<String>?,
    val photos: List<NewPlacePhoto>?,
    val websiteUri: String?,
    val googleMapsUri: String?,
    val regularOpeningHours: OpeningHours?,
    val currentOpeningHours: CurrentOpeningHours?,
    val businessStatus: String?
)

data class OpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)

data class CurrentOpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)

data class DisplayName(
    val text: String?,
    val languageCode: String?
)

data class LocationResult(
    val latitude: Double,
    val longitude: Double
)

data class NewPlacePhoto(
    val name: String?,
    val widthPx: Int?,
    val heightPx: Int?
)

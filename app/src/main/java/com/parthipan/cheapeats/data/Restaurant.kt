package com.parthipan.cheapeats.data

import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val priceLevel: Int,
    val rating: Float,
    val distance: Float, // in miles
    val imageUrl: String?,
    val address: String,
    val location: LatLng,
    val isSponsored: Boolean = false,
    val hasStudentDiscount: Boolean = false,
    val nearTTC: Boolean = false, // Near Toronto Transit Commission stop
    val averagePrice: Float? = null, // Average meal price in dollars
    val websiteUrl: String? = null, // Restaurant website URL
    val googleMapsUrl: String? = null, // Google Maps URL for directions
    val isOpenNow: Boolean? = null, // null = unknown, true = open, false = closed
    val openingHours: String? = null, // Human-readable hours
    val ttcWalkMinutes: Int? = null, // Walking time from nearest station in minutes
    val nearestStation: String? = null, // Nearest TTC station name
    val dataFreshness: DataFreshness = DataFreshness.UNKNOWN, // Data trust level
    val lastVerified: Long? = null // Timestamp when data was last verified
) {
    // Convenience properties for backwards compatibility
    val latitude: Double get() = location.latitude
    val longitude: Double get() = location.longitude

    // Price point display string
    val pricePoint: String
        get() = when (priceLevel) {
            0 -> "Free"
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "$"
        }

    // Check if restaurant is under $15 based on price level or average price
    val isUnder15: Boolean
        get() = averagePrice?.let { it < 15f } ?: (priceLevel <= 1)
}

// Sample data centered around 1200 York Mills Rd, Toronto (43.7615, -79.3456)
val sampleRestaurants = listOf(
    Restaurant(
        id = "1",
        name = "Taco Paradise",
        cuisine = "Mexican",
        priceLevel = 1,
        rating = 4.5f,
        distance = 0.3f,
        imageUrl = null,
        address = "1250 York Mills Rd",
        location = LatLng(43.7620, -79.3440),
        isSponsored = true,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 12.99f,
        isOpenNow = true,
        ttcWalkMinutes = 3,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "2",
        name = "Pasta House",
        cuisine = "Italian",
        priceLevel = 2,
        rating = 4.2f,
        distance = 0.5f,
        imageUrl = null,
        address = "1180 York Mills Rd",
        location = LatLng(43.7608, -79.3470),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 22.50f,
        isOpenNow = true,
        ttcWalkMinutes = 4,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.RECENT
    ),
    Restaurant(
        id = "3",
        name = "Golden Dragon",
        cuisine = "Chinese",
        priceLevel = 1,
        rating = 4.0f,
        distance = 0.8f,
        imageUrl = null,
        address = "1300 York Mills Rd",
        location = LatLng(43.7630, -79.3420),
        isSponsored = true,
        hasStudentDiscount = true,
        nearTTC = false,
        averagePrice = 14.00f,
        isOpenNow = false,
        ttcWalkMinutes = 12,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.CACHED
    ),
    Restaurant(
        id = "4",
        name = "Burger Barn",
        cuisine = "American",
        priceLevel = 1,
        rating = 4.3f,
        distance = 0.4f,
        imageUrl = null,
        address = "1150 York Mills Rd",
        location = LatLng(43.7600, -79.3480),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 11.50f,
        isOpenNow = true,
        ttcWalkMinutes = 5,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "5",
        name = "Sushi Zen",
        cuisine = "Japanese",
        priceLevel = 2,
        rating = 4.7f,
        distance = 1.2f,
        imageUrl = null,
        address = "1400 York Mills Rd",
        location = LatLng(43.7640, -79.3400),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = false,
        averagePrice = 28.00f,
        isOpenNow = null,
        ttcWalkMinutes = 15,
        nearestStation = "Sheppard-Yonge",
        dataFreshness = DataFreshness.UNKNOWN
    ),
    Restaurant(
        id = "6",
        name = "Curry Corner",
        cuisine = "Indian",
        priceLevel = 1,
        rating = 4.4f,
        distance = 0.6f,
        imageUrl = null,
        address = "1100 York Mills Rd",
        location = LatLng(43.7590, -79.3500),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 13.50f,
        isOpenNow = true,
        ttcWalkMinutes = 6,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "7",
        name = "Mediterranean Grill",
        cuisine = "Mediterranean",
        priceLevel = 2,
        rating = 4.6f,
        distance = 0.9f,
        imageUrl = null,
        address = "1350 York Mills Rd",
        location = LatLng(43.7635, -79.3410),
        isSponsored = true,
        hasStudentDiscount = false,
        nearTTC = false,
        averagePrice = 19.99f,
        isOpenNow = true,
        ttcWalkMinutes = 10,
        nearestStation = "Sheppard-Yonge",
        dataFreshness = DataFreshness.RECENT
    ),
    Restaurant(
        id = "8",
        name = "Pho Express",
        cuisine = "Vietnamese",
        priceLevel = 1,
        rating = 4.1f,
        distance = 0.7f,
        imageUrl = null,
        address = "1220 York Mills Rd",
        location = LatLng(43.7612, -79.3460),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 10.99f,
        isOpenNow = true,
        ttcWalkMinutes = 4,
        nearestStation = "York Mills",
        dataFreshness = DataFreshness.LIVE
    )
)

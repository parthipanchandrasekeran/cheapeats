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
    val priceSource: PriceSource = PriceSource.UNKNOWN, // Price confidence level
    val websiteUrl: String? = null, // Restaurant website URL
    val googleMapsUrl: String? = null, // Google Maps URL for directions
    val isOpenNow: Boolean? = null, // null = unknown, true = open, false = closed
    val openingHours: String? = null, // Human-readable hours
    val ttcWalkMinutes: Int? = null, // Walking time from nearest station in minutes
    val nearestStation: String? = null, // Nearest TTC station name
    val dataFreshness: DataFreshness = DataFreshness.UNKNOWN, // Data trust level
    val lastVerified: Long? = null, // Timestamp when data was last verified
    val isFavorite: Boolean = false // User marked as favorite
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

    // Strict mode: verified price under $15
    val isVerifiedUnder15: Boolean
        get() = averagePrice != null &&
                averagePrice <= 15f &&
                priceSource == PriceSource.API_VERIFIED

    // Flexible mode: allows estimated prices up to $17
    val isFlexiblyUnder15: Boolean
        get() = when {
            averagePrice == null -> priceLevel <= 1
            averagePrice <= 17f -> true
            else -> false
        }

    // Price confidence label for UI
    val priceConfidenceLabel: String
        get() = priceSource.toDisplayLabel()
}

// Sample data — real Toronto restaurants matching menu JSON for offline/demo mode
// Centered around downtown Toronto (Bloor/Spadina area: 43.6629, -79.4000)
val sampleRestaurants = listOf(
    Restaurant(
        id = "1",
        name = "Banh Mi Nguyen Huong",
        cuisine = "Vietnamese",
        priceLevel = 1,
        rating = 4.5f,
        distance = 0.2f,
        imageUrl = null,
        address = "322 Spadina Ave, Toronto, ON M5T 2G2",
        location = LatLng(43.6537, -79.3970),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 5.50f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 3,
        nearestStation = "Spadina",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "2",
        name = "Juicy Dumpling",
        cuisine = "Chinese",
        priceLevel = 1,
        rating = 4.3f,
        distance = 0.3f,
        imageUrl = null,
        address = "280 Spadina Ave, Toronto, ON M5T 0A1",
        location = LatLng(43.6524, -79.3965),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 5.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 4,
        nearestStation = "Spadina",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "3",
        name = "Mom's Pan Fried Bun",
        cuisine = "Chinese",
        priceLevel = 1,
        rating = 4.4f,
        distance = 0.4f,
        imageUrl = null,
        address = "189 Dundas St W, Toronto, ON M5G 1C7",
        location = LatLng(43.6534, -79.3867),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 8.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 2,
        nearestStation = "St. Patrick",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "4",
        name = "Salad King",
        cuisine = "Thai",
        priceLevel = 1,
        rating = 4.2f,
        distance = 0.5f,
        imageUrl = null,
        address = "340 Yonge St, Toronto, ON M5B 1R8",
        location = LatLng(43.6569, -79.3822),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 14.50f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 2,
        nearestStation = "Dundas",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "5",
        name = "Jin Dal Lae",
        cuisine = "Korean",
        priceLevel = 1,
        rating = 4.6f,
        distance = 0.8f,
        imageUrl = null,
        address = "647 Bloor St W, Toronto, ON M6G 1L1",
        location = LatLng(43.6629, -79.4137),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 14.95f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 3,
        nearestStation = "Christie",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "6",
        name = "Ghazale",
        cuisine = "Lebanese/Middle Eastern",
        priceLevel = 1,
        rating = 4.3f,
        distance = 0.6f,
        imageUrl = null,
        address = "661 College St, Toronto, ON M6G 1B7",
        location = LatLng(43.6547, -79.4193),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 7.49f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 5,
        nearestStation = "Ossington",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "7",
        name = "Pho Hung",
        cuisine = "Vietnamese",
        priceLevel = 1,
        rating = 4.1f,
        distance = 0.3f,
        imageUrl = null,
        address = "350 Spadina Ave, Toronto, ON M5T 2G4",
        location = LatLng(43.6546, -79.3976),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 12.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 3,
        nearestStation = "Spadina",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "8",
        name = "Jumbo Empanadas",
        cuisine = "Chilean",
        priceLevel = 1,
        rating = 4.4f,
        distance = 0.4f,
        imageUrl = null,
        address = "245 Augusta Ave, Toronto, ON M5T 2L4",
        location = LatLng(43.6554, -79.4010),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 6.50f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 5,
        nearestStation = "Spadina",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "9",
        name = "Rasta Pasta",
        cuisine = "Jamaican-Italian Fusion",
        priceLevel = 1,
        rating = 4.5f,
        distance = 0.5f,
        imageUrl = null,
        address = "61 Kensington Ave, Toronto, ON M5T 2K2",
        location = LatLng(43.6557, -79.4005),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 9.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 6,
        nearestStation = "Spadina",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "10",
        name = "Udupi Palace",
        cuisine = "South Indian Vegetarian",
        priceLevel = 1,
        rating = 4.3f,
        distance = 1.0f,
        imageUrl = null,
        address = "1460 Gerrard St E, Toronto, ON M4L 2A1",
        location = LatLng(43.6681, -79.3289),
        isSponsored = false,
        hasStudentDiscount = true,
        nearTTC = true,
        averagePrice = 10.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 4,
        nearestStation = "Coxwell",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "11",
        name = "Kinton Ramen",
        cuisine = "Japanese Ramen",
        priceLevel = 1,
        rating = 4.4f,
        distance = 0.7f,
        imageUrl = null,
        address = "668 Bloor St W, Toronto, ON M6G 1L2",
        location = LatLng(43.6631, -79.4145),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = true,
        averagePrice = 14.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 3,
        nearestStation = "Christie",
        dataFreshness = DataFreshness.LIVE
    ),
    Restaurant(
        id = "12",
        name = "Hopper Hut",
        cuisine = "Sri Lankan",
        priceLevel = 1,
        rating = 4.2f,
        distance = 1.5f,
        imageUrl = null,
        address = "880 Ellesmere Rd, Scarborough, ON M1P 2W6",
        location = LatLng(43.7738, -79.2765),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = false,
        averagePrice = 7.99f,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = true,
        ttcWalkMinutes = 12,
        nearestStation = "Ellesmere",
        dataFreshness = DataFreshness.LIVE
    )
)

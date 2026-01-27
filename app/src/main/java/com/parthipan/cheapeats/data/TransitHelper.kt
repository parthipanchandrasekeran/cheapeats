package com.parthipan.cheapeats.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Toronto TTC Subway Station data
 */
data class SubwayStation(
    val name: String,
    val location: LatLng,
    val lines: List<String> // e.g., ["Line 1", "Line 2"]
)

/**
 * Helper object for transit-related calculations
 */
object TransitHelper {

    // Default radius for transit accessibility (in meters)
    const val DEFAULT_TRANSIT_RADIUS_METERS = 500.0

    // Greater Toronto Area bounding box (approximate)
    private const val GTA_MIN_LAT = 43.4
    private const val GTA_MAX_LAT = 44.0
    private const val GTA_MIN_LNG = -79.8
    private const val GTA_MAX_LNG = -79.0

    /**
     * Checks if a location is within the Greater Toronto Area.
     * Used to determine if TTC-related features should be enabled.
     *
     * @param location The LatLng coordinates to check
     * @return true if the location is within the GTA bounds
     */
    fun isInTorontoArea(location: LatLng): Boolean {
        return location.latitude in GTA_MIN_LAT..GTA_MAX_LAT &&
               location.longitude in GTA_MIN_LNG..GTA_MAX_LNG
    }

    /**
     * Checks if coordinates are within the Greater Toronto Area.
     *
     * @param latitude The latitude to check
     * @param longitude The longitude to check
     * @return true if the coordinates are within the GTA bounds
     */
    fun isInTorontoArea(latitude: Double, longitude: Double): Boolean {
        return latitude in GTA_MIN_LAT..GTA_MAX_LAT &&
               longitude in GTA_MIN_LNG..GTA_MAX_LNG
    }

    /**
     * Major Toronto TTC Subway Station coordinates
     * These are key interchange/hub stations
     */
    val majorSubwayStations = listOf(
        SubwayStation(
            name = "Union",
            location = LatLng(43.6453, -79.3806),
            lines = listOf("Line 1 Yonge-University")
        ),
        SubwayStation(
            name = "Bloor-Yonge",
            location = LatLng(43.6709, -79.3857),
            lines = listOf("Line 1 Yonge-University", "Line 2 Bloor-Danforth")
        ),
        SubwayStation(
            name = "St. George",
            location = LatLng(43.6682, -79.3998),
            lines = listOf("Line 1 Yonge-University", "Line 2 Bloor-Danforth")
        ),
        SubwayStation(
            name = "Sheppard-Yonge",
            location = LatLng(43.7610, -79.4108),
            lines = listOf("Line 1 Yonge-University", "Line 4 Sheppard")
        )
    )

    /**
     * Extended list of TTC subway stations for more comprehensive coverage
     */
    val allSubwayStations = listOf(
        // Line 1 Yonge-University (South to North on Yonge)
        SubwayStation("Union", LatLng(43.6453, -79.3806), listOf("Line 1")),
        SubwayStation("King", LatLng(43.6490, -79.3780), listOf("Line 1")),
        SubwayStation("Queen", LatLng(43.6523, -79.3791), listOf("Line 1")),
        SubwayStation("Dundas", LatLng(43.6561, -79.3802), listOf("Line 1")),
        SubwayStation("College", LatLng(43.6614, -79.3831), listOf("Line 1")),
        SubwayStation("Wellesley", LatLng(43.6655, -79.3845), listOf("Line 1")),
        SubwayStation("Bloor-Yonge", LatLng(43.6709, -79.3857), listOf("Line 1", "Line 2")),
        SubwayStation("Rosedale", LatLng(43.6772, -79.3889), listOf("Line 1")),
        SubwayStation("Summerhill", LatLng(43.6824, -79.3909), listOf("Line 1")),
        SubwayStation("St. Clair", LatLng(43.6879, -79.3932), listOf("Line 1")),
        SubwayStation("Davisville", LatLng(43.6976, -79.3972), listOf("Line 1")),
        SubwayStation("Eglinton", LatLng(43.7058, -79.3987), listOf("Line 1")),
        SubwayStation("Lawrence", LatLng(43.7250, -79.4023), listOf("Line 1")),
        SubwayStation("York Mills", LatLng(43.7440, -79.4069), listOf("Line 1")),
        SubwayStation("Sheppard-Yonge", LatLng(43.7610, -79.4108), listOf("Line 1", "Line 4")),
        SubwayStation("North York Centre", LatLng(43.7687, -79.4128), listOf("Line 1")),
        SubwayStation("Finch", LatLng(43.7807, -79.4149), listOf("Line 1")),

        // Line 1 University (South to North)
        SubwayStation("St. Andrew", LatLng(43.6476, -79.3847), listOf("Line 1")),
        SubwayStation("Osgoode", LatLng(43.6506, -79.3867), listOf("Line 1")),
        SubwayStation("St. Patrick", LatLng(43.6548, -79.3883), listOf("Line 1")),
        SubwayStation("Queen's Park", LatLng(43.6600, -79.3907), listOf("Line 1")),
        SubwayStation("Museum", LatLng(43.6671, -79.3937), listOf("Line 1")),
        SubwayStation("St. George", LatLng(43.6682, -79.3998), listOf("Line 1", "Line 2")),
        SubwayStation("Spadina", LatLng(43.6673, -79.4038), listOf("Line 1", "Line 2")),
        SubwayStation("Dupont", LatLng(43.6749, -79.4070), listOf("Line 1")),
        SubwayStation("St. Clair West", LatLng(43.6840, -79.4150), listOf("Line 1")),
        SubwayStation("Eglinton West", LatLng(43.6995, -79.4358), listOf("Line 1")),
        SubwayStation("Glencairn", LatLng(43.7090, -79.4410), listOf("Line 1")),
        SubwayStation("Lawrence West", LatLng(43.7157, -79.4443), listOf("Line 1")),
        SubwayStation("Yorkdale", LatLng(43.7245, -79.4476), listOf("Line 1")),
        SubwayStation("Wilson", LatLng(43.7346, -79.4500), listOf("Line 1")),
        SubwayStation("Sheppard West", LatLng(43.7495, -79.4600), listOf("Line 1")),
        SubwayStation("Downsview Park", LatLng(43.7535, -79.4780), listOf("Line 1")),
        SubwayStation("Finch West", LatLng(43.7655, -79.4910), listOf("Line 1")),
        SubwayStation("York University", LatLng(43.7740, -79.4999), listOf("Line 1")),
        SubwayStation("Pioneer Village", LatLng(43.7770, -79.5094), listOf("Line 1")),
        SubwayStation("Highway 407", LatLng(43.7840, -79.5232), listOf("Line 1")),
        SubwayStation("Vaughan Metropolitan Centre", LatLng(43.7942, -79.5273), listOf("Line 1")),

        // Line 2 Bloor-Danforth (West to East)
        SubwayStation("Kipling", LatLng(43.6373, -79.5362), listOf("Line 2")),
        SubwayStation("Islington", LatLng(43.6453, -79.5246), listOf("Line 2")),
        SubwayStation("Royal York", LatLng(43.6485, -79.5113), listOf("Line 2")),
        SubwayStation("Old Mill", LatLng(43.6500, -79.4952), listOf("Line 2")),
        SubwayStation("Jane", LatLng(43.6500, -79.4849), listOf("Line 2")),
        SubwayStation("Runnymede", LatLng(43.6513, -79.4755), listOf("Line 2")),
        SubwayStation("High Park", LatLng(43.6542, -79.4670), listOf("Line 2")),
        SubwayStation("Keele", LatLng(43.6557, -79.4596), listOf("Line 2")),
        SubwayStation("Dundas West", LatLng(43.6569, -79.4528), listOf("Line 2")),
        SubwayStation("Lansdowne", LatLng(43.6594, -79.4429), listOf("Line 2")),
        SubwayStation("Dufferin", LatLng(43.6601, -79.4355), listOf("Line 2")),
        SubwayStation("Ossington", LatLng(43.6624, -79.4265), listOf("Line 2")),
        SubwayStation("Christie", LatLng(43.6642, -79.4184), listOf("Line 2")),
        SubwayStation("Bathurst", LatLng(43.6660, -79.4110), listOf("Line 2")),
        SubwayStation("Bay", LatLng(43.6702, -79.3900), listOf("Line 2")),
        SubwayStation("Sherbourne", LatLng(43.6722, -79.3765), listOf("Line 2")),
        SubwayStation("Castle Frank", LatLng(43.6738, -79.3687), listOf("Line 2")),
        SubwayStation("Broadview", LatLng(43.6769, -79.3587), listOf("Line 2")),
        SubwayStation("Chester", LatLng(43.6783, -79.3520), listOf("Line 2")),
        SubwayStation("Pape", LatLng(43.6799, -79.3450), listOf("Line 2")),
        SubwayStation("Donlands", LatLng(43.6812, -79.3378), listOf("Line 2")),
        SubwayStation("Greenwood", LatLng(43.6828, -79.3305), listOf("Line 2")),
        SubwayStation("Coxwell", LatLng(43.6843, -79.3232), listOf("Line 2")),
        SubwayStation("Woodbine", LatLng(43.6865, -79.3128), listOf("Line 2")),
        SubwayStation("Main Street", LatLng(43.6890, -79.3018), listOf("Line 2")),
        SubwayStation("Victoria Park", LatLng(43.6953, -79.2930), listOf("Line 2")),
        SubwayStation("Warden", LatLng(43.7114, -79.2799), listOf("Line 2")),
        SubwayStation("Kennedy", LatLng(43.7325, -79.2636), listOf("Line 2", "Line 3")),

        // Line 4 Sheppard
        SubwayStation("Bayview", LatLng(43.7669, -79.3868), listOf("Line 4")),
        SubwayStation("Bessarion", LatLng(43.7693, -79.3764), listOf("Line 4")),
        SubwayStation("Leslie", LatLng(43.7710, -79.3657), listOf("Line 4")),
        SubwayStation("Don Mills", LatLng(43.7756, -79.3460), listOf("Line 4"))
    )

    /**
     * Calculates if a restaurant's coordinates are within a specified radius
     * of any TTC subway station.
     *
     * @param restaurantLocation The LatLng coordinates of the restaurant
     * @param radiusMeters The radius in meters to check (default: 500m)
     * @param stations The list of subway stations to check against (default: all stations)
     * @return true if the restaurant is within the radius of any station
     */
    fun isTransitAccessible(
        restaurantLocation: LatLng,
        radiusMeters: Double = DEFAULT_TRANSIT_RADIUS_METERS,
        stations: List<SubwayStation> = allSubwayStations
    ): Boolean {
        return stations.any { station ->
            calculateDistanceMeters(restaurantLocation, station.location) <= radiusMeters
        }
    }

    /**
     * Finds the nearest subway station to the given location.
     *
     * @param location The LatLng coordinates to check
     * @param stations The list of subway stations to search (default: all stations)
     * @return Pair of the nearest SubwayStation and distance in meters, or null if no stations
     */
    fun findNearestStation(
        location: LatLng,
        stations: List<SubwayStation> = allSubwayStations
    ): Pair<SubwayStation, Float>? {
        return stations
            .map { station -> station to calculateDistanceMeters(location, station.location) }
            .minByOrNull { it.second }
    }

    /**
     * Gets all subway stations within a specified radius of the location.
     *
     * @param location The LatLng coordinates to check
     * @param radiusMeters The radius in meters
     * @param stations The list of subway stations to search (default: all stations)
     * @return List of stations within the radius, sorted by distance
     */
    fun getStationsWithinRadius(
        location: LatLng,
        radiusMeters: Double = DEFAULT_TRANSIT_RADIUS_METERS,
        stations: List<SubwayStation> = allSubwayStations
    ): List<Pair<SubwayStation, Float>> {
        return stations
            .map { station -> station to calculateDistanceMeters(location, station.location) }
            .filter { it.second <= radiusMeters }
            .sortedBy { it.second }
    }

    /**
     * Calculates the distance in meters between two LatLng points
     * using Android's Location.distanceBetween method.
     *
     * @param from Starting coordinates
     * @param to Ending coordinates
     * @return Distance in meters
     */
    fun calculateDistanceMeters(from: LatLng, to: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0]
    }

    /**
     * Checks if a location is near a major hub station (Union, Bloor-Yonge, St. George, Sheppard-Yonge)
     *
     * @param location The LatLng coordinates to check
     * @param radiusMeters The radius in meters (default: 500m)
     * @return true if within radius of a major hub
     */
    fun isNearMajorHub(
        location: LatLng,
        radiusMeters: Double = DEFAULT_TRANSIT_RADIUS_METERS
    ): Boolean {
        return isTransitAccessible(location, radiusMeters, majorSubwayStations)
    }

    // Walking speed in meters per minute (average walking pace ~5 km/h)
    private const val WALKING_SPEED_M_PER_MIN = 80f

    /**
     * Calculate walking time in minutes from distance in meters.
     * Uses average walking speed of 80 meters per minute (~5 km/h).
     *
     * @param distanceMeters Distance in meters
     * @return Walking time in minutes
     */
    fun walkingTimeMinutes(distanceMeters: Float): Int {
        return (distanceMeters / WALKING_SPEED_M_PER_MIN).toInt()
    }

    /**
     * Get walking time to nearest station from a given location.
     *
     * @param location The LatLng coordinates to check
     * @return Walking time in minutes to nearest station, or null if no stations available
     */
    fun walkingTimeToNearestStation(location: LatLng): Int? {
        return findNearestStation(location)?.let { (_, distanceMeters) ->
            walkingTimeMinutes(distanceMeters)
        }
    }
}

/**
 * Extension function for Restaurant to check transit accessibility
 */
fun Restaurant.isTransitAccessible(radiusMeters: Double = TransitHelper.DEFAULT_TRANSIT_RADIUS_METERS): Boolean {
    return TransitHelper.isTransitAccessible(this.location, radiusMeters)
}

/**
 * Extension function for Restaurant to find nearest station
 */
fun Restaurant.findNearestStation(): Pair<SubwayStation, Float>? {
    return TransitHelper.findNearestStation(this.location)
}

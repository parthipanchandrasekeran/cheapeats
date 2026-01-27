package com.parthipan.cheapeats.data.favorites

import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.database.ViewHistoryDao

/**
 * Prevents showing the same restaurant repeatedly in recommendations.
 * Restaurants accessed via search are NOT filtered.
 */
class RepeatProtection(
    private val viewHistoryDao: ViewHistoryDao
) {
    companion object {
        const val COOLDOWN_HOURS = 24
        const val MAX_REPEATS_PER_DAY = 2
    }

    /**
     * Filter out restaurants that were recently shown in recommendations.
     * Restaurants accessed via search are NOT filtered.
     */
    suspend fun filterRecentlyShown(
        restaurants: List<Restaurant>
    ): List<Restaurant> {
        val since = System.currentTimeMillis() - (COOLDOWN_HOURS * 3600000L)
        val recentIds = viewHistoryDao.getRecentlyRecommended(since).toSet()

        return restaurants.filter { restaurant ->
            restaurant.id !in recentIds
        }
    }

    /**
     * Record that a restaurant was shown/viewed.
     */
    suspend fun recordView(restaurantId: String, source: ViewSource) {
        viewHistoryDao.recordView(
            ViewHistoryEntry(
                restaurantId = restaurantId,
                source = source
            )
        )
    }

    /**
     * Cleanup old history entries (call periodically).
     */
    suspend fun cleanup() {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 3600000L)
        viewHistoryDao.cleanupOldHistory(weekAgo)
    }
}

package com.parthipan.cheapeats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parthipan.cheapeats.data.cache.CachedRestaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {

    @Query("""
        SELECT * FROM cached_restaurants
        ORDER BY lastAccessedAt DESC
        LIMIT :limit
    """)
    fun getRecentlyViewed(limit: Int = 50): Flow<List<CachedRestaurant>>

    // Longitude correction: 0.722 ≈ cos(43.7°) for Toronto latitude.
    // Without this, the search area is an east-west stretched ellipse.
    @Query("""
        SELECT * FROM cached_restaurants
        WHERE (
            (:lat - latitude) * (:lat - latitude) +
            ((:lng - longitude) * 0.722) * ((:lng - longitude) * 0.722)
        ) < :radiusSquared
        ORDER BY rating DESC
        LIMIT :limit
    """)
    fun getNearby(
        lat: Double,
        lng: Double,
        radiusSquared: Double = 0.001,
        limit: Int = 30
    ): Flow<List<CachedRestaurant>>

    @Query("""
        SELECT * FROM cached_restaurants
        WHERE averagePrice IS NOT NULL AND averagePrice < 15.0
        ORDER BY averagePrice ASC
        LIMIT :limit
    """)
    fun getCheapestCached(limit: Int = 20): Flow<List<CachedRestaurant>>

    @Query("SELECT * FROM cached_restaurants WHERE id = :id")
    suspend fun getCachedRestaurant(id: String): CachedRestaurant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRestaurant(restaurant: CachedRestaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRestaurants(restaurants: List<CachedRestaurant>)

    @Query("UPDATE cached_restaurants SET lastAccessedAt = :now WHERE id = :id")
    suspend fun touchRestaurant(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE cached_restaurants SET thumbnailPath = :path WHERE id = :id")
    suspend fun updateThumbnailPath(id: String, path: String)

    @Query("DELETE FROM cached_restaurants WHERE cachedAt < :before")
    suspend fun cleanupOldCache(before: Long)

    @Query("DELETE FROM cached_restaurants WHERE id = :id")
    suspend fun deleteCached(id: String)

    @Query("DELETE FROM cached_restaurants")
    suspend fun clearAllCache()

    @Query("SELECT COUNT(*) FROM cached_restaurants")
    suspend fun getCacheCount(): Int

    @Query("SELECT SUM(LENGTH(thumbnailPath)) FROM cached_restaurants WHERE thumbnailPath IS NOT NULL")
    suspend fun getCachedImageSize(): Long?
}

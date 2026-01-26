package com.parthipan.cheapeats.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Restaurant operations.
 */
@Dao
interface RestaurantDao {

    // ============== INSERT OPERATIONS ==============

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restaurant: RestaurantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<RestaurantEntity>): List<Long>

    // ============== UPDATE OPERATIONS ==============

    @Update
    suspend fun update(restaurant: RestaurantEntity)

    @Query("UPDATE restaurants SET is_favorite = :isFavorite WHERE id = :restaurantId")
    suspend fun updateFavoriteStatus(restaurantId: Long, isFavorite: Boolean)

    @Query("UPDATE restaurants SET last_updated = :timestamp WHERE id = :restaurantId")
    suspend fun updateLastUpdated(restaurantId: Long, timestamp: Long = System.currentTimeMillis())

    // ============== DELETE OPERATIONS ==============

    @Delete
    suspend fun delete(restaurant: RestaurantEntity)

    @Query("DELETE FROM restaurants WHERE id = :restaurantId")
    suspend fun deleteById(restaurantId: Long)

    @Query("DELETE FROM restaurants")
    suspend fun deleteAll()

    // ============== QUERY OPERATIONS ==============

    @Query("SELECT * FROM restaurants WHERE id = :restaurantId")
    suspend fun getById(restaurantId: Long): RestaurantEntity?

    @Query("SELECT * FROM restaurants WHERE place_id = :placeId")
    suspend fun getByPlaceId(placeId: String): RestaurantEntity?

    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE near_ttc = 1 ORDER BY name ASC")
    fun getTransitAccessibleRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE price_level <= :maxPriceLevel ORDER BY price_level ASC")
    fun getRestaurantsByMaxPrice(maxPriceLevel: Int): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE has_student_discount = 1 ORDER BY name ASC")
    fun getRestaurantsWithStudentDiscount(): Flow<List<RestaurantEntity>>

    @Query("""
        SELECT * FROM restaurants
        WHERE (:cuisine IS NULL OR cuisine = :cuisine)
        AND (:maxPrice IS NULL OR price_level <= :maxPrice)
        AND (:nearTTC IS NULL OR near_ttc = :nearTTC)
        AND (:hasStudentDiscount IS NULL OR has_student_discount = :hasStudentDiscount)
        ORDER BY
            CASE WHEN is_sponsored = 1 THEN 0 ELSE 1 END,
            rating DESC
    """)
    fun getFilteredRestaurants(
        cuisine: String? = null,
        maxPrice: Int? = null,
        nearTTC: Boolean? = null,
        hasStudentDiscount: Boolean? = null
    ): Flow<List<RestaurantEntity>>

    // ============== RELATIONSHIP QUERIES ==============

    @Transaction
    @Query("SELECT * FROM restaurants WHERE id = :restaurantId")
    suspend fun getRestaurantWithSpecials(restaurantId: Long): RestaurantWithSpecials?

    @Transaction
    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    fun getAllRestaurantsWithSpecials(): Flow<List<RestaurantWithSpecials>>

    @Transaction
    @Query("""
        SELECT DISTINCT r.* FROM restaurants r
        INNER JOIN daily_specials ds ON r.id = ds.restaurant_id
        WHERE ds.day_of_week = :dayOfWeek AND ds.is_active = 1
        ORDER BY r.is_sponsored DESC, r.rating DESC
    """)
    fun getRestaurantsWithSpecialsForDay(dayOfWeek: DayOfWeek): Flow<List<RestaurantWithSpecials>>

    // ============== SEARCH ==============

    @Query("""
        SELECT * FROM restaurants
        WHERE name LIKE '%' || :query || '%'
        OR cuisine LIKE '%' || :query || '%'
        OR address LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
            rating DESC
    """)
    fun searchRestaurants(query: String): Flow<List<RestaurantEntity>>

    // ============== STATISTICS ==============

    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun getRestaurantCount(): Int

    @Query("SELECT COUNT(*) FROM restaurants WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT DISTINCT cuisine FROM restaurants ORDER BY cuisine ASC")
    suspend fun getAllCuisines(): List<String>
}

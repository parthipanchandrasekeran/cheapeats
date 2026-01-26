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
 * Data Access Object for DailySpecial operations.
 *
 * Optimized queries for efficiently pulling "specials happening today"
 * using indexed day_of_week column.
 */
@Dao
interface DailySpecialDao {

    // ============== INSERT OPERATIONS ==============

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(special: DailySpecialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(specials: List<DailySpecialEntity>): List<Long>

    // ============== UPDATE OPERATIONS ==============

    @Update
    suspend fun update(special: DailySpecialEntity)

    @Query("UPDATE daily_specials SET is_active = :isActive WHERE id = :specialId")
    suspend fun updateActiveStatus(specialId: Long, isActive: Boolean)

    @Query("UPDATE daily_specials SET updated_at = :timestamp WHERE id = :specialId")
    suspend fun updateTimestamp(specialId: Long, timestamp: Long = System.currentTimeMillis())

    // ============== DELETE OPERATIONS ==============

    @Delete
    suspend fun delete(special: DailySpecialEntity)

    @Query("DELETE FROM daily_specials WHERE id = :specialId")
    suspend fun deleteById(specialId: Long)

    @Query("DELETE FROM daily_specials WHERE restaurant_id = :restaurantId")
    suspend fun deleteAllForRestaurant(restaurantId: Long)

    @Query("DELETE FROM daily_specials")
    suspend fun deleteAll()

    // ============== TODAY'S SPECIALS QUERIES (OPTIMIZED) ==============

    /**
     * Get all active specials for today.
     * Uses indexed day_of_week column for efficient lookup.
     * Sorted by sponsored status (sponsored first) and price.
     */
    @Query("""
        SELECT ds.* FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        ORDER BY r.is_sponsored DESC, ds.special_price ASC
    """)
    fun getTodaysSpecials(today: DayOfWeek = DayOfWeek.today()): Flow<List<DailySpecialEntity>>

    /**
     * Get today's specials with full restaurant information.
     * Efficient join query using indexed columns.
     */
    @Transaction
    @Query("""
        SELECT ds.* FROM daily_specials ds
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        ORDER BY ds.special_price ASC
    """)
    fun getTodaysSpecialsWithRestaurant(today: DayOfWeek = DayOfWeek.today()): Flow<List<SpecialWithRestaurant>>

    /**
     * Get today's specials as lightweight summary objects.
     * Optimal for list displays - avoids loading unnecessary data.
     */
    @Query("""
        SELECT
            ds.id AS specialId,
            ds.restaurant_id AS restaurantId,
            r.name AS restaurantName,
            ds.special_name AS specialName,
            ds.description AS description,
            ds.special_price AS specialPrice,
            ds.original_price AS originalPrice,
            ds.category AS category,
            ds.start_time AS startTime,
            ds.end_time AS endTime,
            r.cuisine AS restaurantCuisine,
            r.address AS restaurantAddress,
            r.near_ttc AS nearTTC
        FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        ORDER BY r.is_sponsored DESC, ds.special_price ASC
    """)
    fun getTodaysSpecialsSummary(today: DayOfWeek = DayOfWeek.today()): Flow<List<TodaysSpecialSummary>>

    /**
     * Get today's specials filtered by category.
     */
    @Query("""
        SELECT ds.* FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        AND ds.category = :category
        ORDER BY r.is_sponsored DESC, ds.special_price ASC
    """)
    fun getTodaysSpecialsByCategory(
        category: SpecialCategory,
        today: DayOfWeek = DayOfWeek.today()
    ): Flow<List<DailySpecialEntity>>

    /**
     * Get today's specials under a certain price.
     */
    @Query("""
        SELECT ds.* FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        AND ds.special_price <= :maxPrice
        ORDER BY ds.special_price ASC
    """)
    fun getTodaysSpecialsUnderPrice(
        maxPrice: Float,
        today: DayOfWeek = DayOfWeek.today()
    ): Flow<List<DailySpecialEntity>>

    /**
     * Get today's specials near TTC.
     */
    @Query("""
        SELECT ds.* FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        AND r.near_ttc = 1
        ORDER BY r.is_sponsored DESC, ds.special_price ASC
    """)
    fun getTodaysSpecialsNearTTC(today: DayOfWeek = DayOfWeek.today()): Flow<List<DailySpecialEntity>>

    /**
     * Count of today's specials - useful for badges/notifications.
     */
    @Query("""
        SELECT COUNT(*) FROM daily_specials
        WHERE day_of_week = :today AND is_active = 1
    """)
    suspend fun getTodaysSpecialsCount(today: DayOfWeek = DayOfWeek.today()): Int

    // ============== DAY-SPECIFIC QUERIES ==============

    @Query("""
        SELECT * FROM daily_specials
        WHERE day_of_week = :dayOfWeek AND is_active = 1
        ORDER BY special_price ASC
    """)
    fun getSpecialsForDay(dayOfWeek: DayOfWeek): Flow<List<DailySpecialEntity>>

    @Transaction
    @Query("""
        SELECT * FROM daily_specials
        WHERE day_of_week = :dayOfWeek AND is_active = 1
        ORDER BY special_price ASC
    """)
    fun getSpecialsForDayWithRestaurant(dayOfWeek: DayOfWeek): Flow<List<SpecialWithRestaurant>>

    // ============== RESTAURANT-SPECIFIC QUERIES ==============

    @Query("SELECT * FROM daily_specials WHERE restaurant_id = :restaurantId ORDER BY day_of_week ASC")
    fun getSpecialsForRestaurant(restaurantId: Long): Flow<List<DailySpecialEntity>>

    @Query("""
        SELECT * FROM daily_specials
        WHERE restaurant_id = :restaurantId
        AND day_of_week = :dayOfWeek
        AND is_active = 1
    """)
    suspend fun getRestaurantSpecialsForDay(restaurantId: Long, dayOfWeek: DayOfWeek): List<DailySpecialEntity>

    @Query("""
        SELECT * FROM daily_specials
        WHERE restaurant_id = :restaurantId
        AND day_of_week = :today
        AND is_active = 1
    """)
    suspend fun getRestaurantTodaysSpecials(
        restaurantId: Long,
        today: DayOfWeek = DayOfWeek.today()
    ): List<DailySpecialEntity>

    // ============== SINGLE ITEM QUERIES ==============

    @Query("SELECT * FROM daily_specials WHERE id = :specialId")
    suspend fun getById(specialId: Long): DailySpecialEntity?

    @Transaction
    @Query("SELECT * FROM daily_specials WHERE id = :specialId")
    suspend fun getByIdWithRestaurant(specialId: Long): SpecialWithRestaurant?

    // ============== STATISTICS ==============

    @Query("SELECT COUNT(*) FROM daily_specials WHERE is_active = 1")
    suspend fun getActiveSpecialsCount(): Int

    @Query("""
        SELECT COUNT(*) FROM daily_specials ds
        INNER JOIN restaurants r ON ds.restaurant_id = r.id
        WHERE ds.day_of_week = :today
        AND ds.is_active = 1
        AND r.near_ttc = 1
    """)
    suspend fun getTodaysSpecialsNearTTCCount(today: DayOfWeek = DayOfWeek.today()): Int

    @Query("SELECT DISTINCT category FROM daily_specials WHERE is_active = 1")
    suspend fun getActiveCategories(): List<SpecialCategory>

    @Query("SELECT MIN(special_price) FROM daily_specials WHERE day_of_week = :today AND is_active = 1")
    suspend fun getCheapestTodaysSpecialPrice(today: DayOfWeek = DayOfWeek.today()): Float?

    @Query("SELECT MAX(special_price) FROM daily_specials WHERE day_of_week = :today AND is_active = 1")
    suspend fun getMostExpensiveTodaysSpecialPrice(today: DayOfWeek = DayOfWeek.today()): Float?
}

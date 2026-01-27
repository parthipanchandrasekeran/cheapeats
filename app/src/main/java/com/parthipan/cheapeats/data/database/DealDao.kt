package com.parthipan.cheapeats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parthipan.cheapeats.data.deals.Deal
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {

    @Query("""
        SELECT * FROM deals
        WHERE restaurantId = :restaurantId
        AND dealPrice < 15.0
        AND (validUntil IS NULL OR validUntil > :now)
        ORDER BY dealPrice ASC
    """)
    fun getDealsForRestaurant(restaurantId: String, now: Long): Flow<List<Deal>>

    @Query("""
        SELECT * FROM deals
        WHERE dealPrice < 15.0
        AND (validUntil IS NULL OR validUntil > :now)
        AND (
            validDays & :todayBitmask > 0
            OR validDays = 0
            OR validDays = 127
        )
        ORDER BY
            CASE WHEN validUntil IS NOT NULL THEN 0 ELSE 1 END,
            validUntil ASC,
            dealPrice ASC
        LIMIT :limit
    """)
    fun getActiveDealsToday(
        now: Long,
        todayBitmask: Int,
        limit: Int = 20
    ): Flow<List<Deal>>

    @Query("""
        SELECT * FROM deals
        WHERE validUntil IS NOT NULL
        AND validUntil > :now
        AND validUntil < :soonThreshold
        ORDER BY validUntil ASC
    """)
    fun getExpiringDeals(now: Long, soonThreshold: Long): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE id = :dealId")
    suspend fun getDealById(dealId: String): Deal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: Deal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeals(deals: List<Deal>)

    @Query("UPDATE deals SET upvotes = upvotes + 1 WHERE id = :dealId")
    suspend fun upvoteDeal(dealId: String)

    @Query("UPDATE deals SET downvotes = downvotes + 1 WHERE id = :dealId")
    suspend fun downvoteDeal(dealId: String)

    @Query("UPDATE deals SET reportCount = reportCount + 1 WHERE id = :dealId")
    suspend fun reportDeal(dealId: String)

    @Query("DELETE FROM deals WHERE validUntil < :now")
    suspend fun cleanupExpiredDeals(now: Long)

    @Query("DELETE FROM deals WHERE id = :dealId")
    suspend fun deleteDeal(dealId: String)

    @Query("SELECT COUNT(*) FROM deals WHERE dealPrice < 15.0")
    suspend fun getActiveDealCount(): Int
}

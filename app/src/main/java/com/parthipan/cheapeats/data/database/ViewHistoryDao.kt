package com.parthipan.cheapeats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.parthipan.cheapeats.data.favorites.ViewHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ViewHistoryDao {

    @Insert
    suspend fun recordView(entry: ViewHistoryEntry)

    @Query("""
        SELECT restaurantId FROM view_history
        WHERE viewedAt > :since
        AND source = 'RECOMMENDATION'
    """)
    suspend fun getRecentlyRecommended(since: Long): List<String>

    @Query("""
        SELECT restaurantId FROM view_history
        WHERE viewedAt > :since
        ORDER BY viewedAt DESC
    """)
    suspend fun getRecentlyViewed(since: Long): List<String>

    @Query("""
        SELECT * FROM view_history
        WHERE restaurantId = :restaurantId
        ORDER BY viewedAt DESC
        LIMIT 1
    """)
    suspend fun getLastView(restaurantId: String): ViewHistoryEntry?

    @Query("""
        SELECT COUNT(*) FROM view_history
        WHERE restaurantId = :restaurantId
        AND viewedAt > :since
    """)
    suspend fun getViewCount(restaurantId: String, since: Long): Int

    @Query("DELETE FROM view_history WHERE viewedAt < :before")
    suspend fun cleanupOldHistory(before: Long)

    @Query("SELECT COUNT(*) FROM view_history")
    suspend fun getTotalViewCount(): Int

    @Query("""
        SELECT restaurantId, COUNT(*) as viewCount
        FROM view_history
        WHERE viewedAt > :since
        GROUP BY restaurantId
        ORDER BY viewCount DESC
        LIMIT :limit
    """)
    suspend fun getMostViewed(since: Long, limit: Int = 10): List<ViewCountResult>
}

data class ViewCountResult(
    val restaurantId: String,
    val viewCount: Int
)

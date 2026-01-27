package com.parthipan.cheapeats.data.deals

import com.parthipan.cheapeats.data.database.DealDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing deals.
 */
class DealRepository(
    private val dealDao: DealDao
) {
    /**
     * Get all active deals for today.
     */
    fun getActiveDealsToday(limit: Int = 20): Flow<List<Deal>> {
        val now = System.currentTimeMillis()
        val todayMask = DealTimeHelper.getTodayBitmask()
        return dealDao.getActiveDealsToday(now, todayMask, limit)
            .map { deals ->
                deals.filter { DealTimeHelper.isDealActiveNow(it) }
            }
    }

    /**
     * Get deals for a specific restaurant.
     */
    fun getDealsForRestaurant(restaurantId: String): Flow<List<Deal>> {
        val now = System.currentTimeMillis()
        return dealDao.getDealsForRestaurant(restaurantId, now)
    }

    /**
     * Get deals that are expiring soon (within next few hours).
     */
    fun getExpiringDeals(withinHours: Int = 3): Flow<List<Deal>> {
        val now = System.currentTimeMillis()
        val soonThreshold = now + (withinHours * 3600000L)
        return dealDao.getExpiringDeals(now, soonThreshold)
    }

    /**
     * Submit a new deal (user-submitted).
     */
    suspend fun submitDeal(submission: DealSubmission): Result<Deal> {
        // Validation
        if (submission.dealPrice >= 15f) {
            return Result.failure(IllegalArgumentException("Deal must be under $15"))
        }

        if (submission.title.length < 5) {
            return Result.failure(IllegalArgumentException("Title too short"))
        }

        val deal = Deal(
            restaurantId = submission.restaurantId,
            restaurantName = submission.restaurantName,
            title = submission.title,
            description = submission.description,
            originalPrice = submission.originalPrice,
            dealPrice = submission.dealPrice,
            dealType = submission.dealType,
            source = DealSource.USER_SUBMITTED,
            validDays = submission.validDays,
            startTime = submission.startTime,
            endTime = submission.endTime,
            validUntil = submission.validUntil
        )

        dealDao.insertDeal(deal)
        return Result.success(deal)
    }

    /**
     * Upvote a deal.
     */
    suspend fun upvoteDeal(dealId: String) {
        dealDao.upvoteDeal(dealId)
    }

    /**
     * Downvote a deal.
     */
    suspend fun downvoteDeal(dealId: String) {
        dealDao.downvoteDeal(dealId)
    }

    /**
     * Report a deal as invalid/spam.
     */
    suspend fun reportDeal(dealId: String) {
        dealDao.reportDeal(dealId)
    }

    /**
     * Clean up expired deals.
     */
    suspend fun cleanupExpiredDeals() {
        dealDao.cleanupExpiredDeals(System.currentTimeMillis())
    }

    /**
     * Get the count of active deals.
     */
    suspend fun getActiveDealCount(): Int {
        return dealDao.getActiveDealCount()
    }
}

/**
 * Data class for submitting a new deal.
 */
data class DealSubmission(
    val restaurantId: String,
    val restaurantName: String,
    val title: String,
    val dealPrice: Float,
    val originalPrice: Float? = null,
    val description: String? = null,
    val dealType: DealType = DealType.DAILY_SPECIAL,
    val validDays: Int = DealTimeHelper.ALL_DAYS,
    val startTime: String? = null,
    val endTime: String? = null,
    val validUntil: Long? = null
)

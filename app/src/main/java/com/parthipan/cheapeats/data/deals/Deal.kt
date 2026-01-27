package com.parthipan.cheapeats.data.deals

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a deal or daily special at a restaurant.
 */
@Entity(
    tableName = "deals",
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["dealPrice"]),
        Index(value = ["validUntil"]),
        Index(value = ["validDays"]),
        Index(value = ["dealType"])
    ]
)
data class Deal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val restaurantId: String,
    val restaurantName: String,

    val title: String,
    val description: String?,
    val originalPrice: Float?,
    val dealPrice: Float,

    val dealType: DealType,
    val source: DealSource,

    // Time constraints - validDays is a bitmask
    val validDays: Int = DealTimeHelper.ALL_DAYS,
    val startTime: String? = null,
    val endTime: String? = null,
    val validFrom: Long? = null,
    val validUntil: Long? = null,

    // Metadata
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val reportCount: Int = 0,
    val submittedBy: String? = null,
    val verifiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val netVotes: Int get() = upvotes - downvotes

    val isUserSubmitted: Boolean get() = source == DealSource.USER_SUBMITTED

    val savingsAmount: Float? get() = originalPrice?.let { it - dealPrice }

    val savingsPercent: Int? get() = originalPrice?.let {
        ((it - dealPrice) / it * 100).toInt()
    }
}

enum class DealType {
    DAILY_SPECIAL,
    WEEKLY_SPECIAL,
    LIMITED_TIME,
    STUDENT_DISCOUNT,
    HAPPY_HOUR,
    COMBO_DEAL
}

enum class DealSource {
    OFFICIAL,
    USER_SUBMITTED,
    SCRAPED,
    VERIFIED
}

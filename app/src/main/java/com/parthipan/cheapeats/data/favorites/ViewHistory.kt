package com.parthipan.cheapeats.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Records when a user viewed/interacted with a restaurant.
 * Used for repeat protection in recommendations.
 */
@Entity(tableName = "view_history")
data class ViewHistoryEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val restaurantId: String,
    val viewedAt: Long = System.currentTimeMillis(),
    val source: ViewSource
)

/**
 * How the user encountered the restaurant.
 */
enum class ViewSource {
    SEARCH,
    RECOMMENDATION,
    MAP_TAP,
    COLLECTION,
    DEAL
}

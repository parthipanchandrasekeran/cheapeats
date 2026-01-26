package com.parthipan.cheapeats.database

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a Restaurant with all its DailySpecials.
 * Used for one-to-many relationship queries.
 */
data class RestaurantWithSpecials(
    @Embedded
    val restaurant: RestaurantEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "restaurant_id"
    )
    val specials: List<DailySpecialEntity>
) {
    /**
     * Get specials for a specific day
     */
    fun specialsForDay(day: DayOfWeek): List<DailySpecialEntity> {
        return specials.filter { it.dayOfWeek == day && it.isActive }
    }

    /**
     * Get today's specials
     */
    fun todaysSpecials(): List<DailySpecialEntity> {
        return specialsForDay(DayOfWeek.today())
    }

    /**
     * Check if restaurant has any specials today
     */
    fun hasSpecialsToday(): Boolean {
        return todaysSpecials().isNotEmpty()
    }
}

/**
 * Data class representing a DailySpecial with its parent Restaurant.
 * Useful for displaying special details with restaurant info.
 */
data class SpecialWithRestaurant(
    @Embedded
    val special: DailySpecialEntity,

    @Relation(
        parentColumn = "restaurant_id",
        entityColumn = "id"
    )
    val restaurant: RestaurantEntity
)

/**
 * Lightweight data class for displaying today's specials in a list.
 * Avoids loading full entity data when not needed.
 */
data class TodaysSpecialSummary(
    val specialId: Long,
    val restaurantId: Long,
    val restaurantName: String,
    val specialName: String,
    val description: String,
    val specialPrice: Float,
    val originalPrice: Float?,
    val category: SpecialCategory,
    val startTime: String?,
    val endTime: String?,
    val restaurantCuisine: String,
    val restaurantAddress: String,
    val nearTTC: Boolean
) {
    val savings: Float?
        get() = originalPrice?.let { it - specialPrice }

    val discountPercentage: Int?
        get() = originalPrice?.let {
            ((it - specialPrice) / it * 100).toInt()
        }

    val timeRangeDisplay: String
        get() = when {
            startTime == null && endTime == null -> "All Day"
            startTime != null && endTime != null -> "$startTime - $endTime"
            startTime != null -> "From $startTime"
            endTime != null -> "Until $endTime"
            else -> "All Day"
        }
}

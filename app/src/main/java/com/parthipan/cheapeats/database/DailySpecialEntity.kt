package com.parthipan.cheapeats.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a daily special offered by a restaurant.
 *
 * Has a many-to-one relationship with RestaurantEntity.
 * A restaurant can have multiple daily specials (one for each day, or multiple per day).
 *
 * Indexed on:
 * - day_of_week: Critical for efficient "today's specials" queries
 * - restaurant_id: For joining with restaurants
 * - is_active: For filtering active specials only
 */
@Entity(
    tableName = "daily_specials",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurant_id"],
            onDelete = ForeignKey.CASCADE, // Delete specials when restaurant is deleted
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["day_of_week"]), // Primary index for today's specials query
        Index(value = ["restaurant_id"]),
        Index(value = ["is_active"]),
        Index(value = ["day_of_week", "is_active"]) // Composite index for filtered queries
    ]
)
data class DailySpecialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "restaurant_id")
    val restaurantId: Long,

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: DayOfWeek,

    @ColumnInfo(name = "special_name")
    val specialName: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "special_price")
    val specialPrice: Float,

    @ColumnInfo(name = "original_price")
    val originalPrice: Float? = null, // For showing savings

    @ColumnInfo(name = "category")
    val category: SpecialCategory = SpecialCategory.FOOD,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "start_time")
    val startTime: String? = null, // e.g., "11:00" - null means all day

    @ColumnInfo(name = "end_time")
    val endTime: String? = null, // e.g., "15:00" - null means all day

    @ColumnInfo(name = "terms_conditions")
    val termsConditions: String? = null, // e.g., "Dine-in only", "With student ID"

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate savings if original price is available
     */
    val savings: Float?
        get() = originalPrice?.let { it - specialPrice }

    /**
     * Calculate discount percentage if original price is available
     */
    val discountPercentage: Int?
        get() = originalPrice?.let {
            ((it - specialPrice) / it * 100).toInt()
        }

    /**
     * Check if this special is available all day
     */
    val isAllDay: Boolean
        get() = startTime == null && endTime == null

    /**
     * Format time range for display
     */
    val timeRangeDisplay: String
        get() = when {
            isAllDay -> "All Day"
            startTime != null && endTime != null -> "$startTime - $endTime"
            startTime != null -> "From $startTime"
            endTime != null -> "Until $endTime"
            else -> "All Day"
        }
}

/**
 * Category of the daily special
 */
enum class SpecialCategory(val displayName: String) {
    FOOD("Food"),
    DRINK("Drink"),
    COMBO("Combo"),
    APPETIZER("Appetizer"),
    DESSERT("Dessert"),
    HAPPY_HOUR("Happy Hour"),
    BRUNCH("Brunch"),
    LUNCH("Lunch"),
    DINNER("Dinner")
}

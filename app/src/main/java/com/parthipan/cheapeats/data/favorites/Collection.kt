package com.parthipan.cheapeats.data.favorites

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A collection of restaurants (upgraded from simple favorites).
 */
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val colorHex: String,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Junction table for many-to-many relationship between collections and restaurants.
 */
@Entity(
    tableName = "collection_restaurants",
    primaryKeys = ["collectionId", "restaurantId"],
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["addedAt"])
    ]
)
data class CollectionRestaurant(
    val collectionId: String,
    val restaurantId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val note: String? = null
)

/**
 * Collection with its restaurant count.
 */
data class CollectionWithCount(
    @Embedded val collection: Collection,
    val restaurantCount: Int
)

/**
 * Predefined system collections.
 */
object SystemCollections {
    val FAVORITES = Collection(
        id = "favorites",
        name = "Favorites",
        icon = "favorite",
        colorHex = "#F44336",
        isSystem = true,
        sortOrder = 0
    )

    val LUNCH = Collection(
        id = "lunch",
        name = "Lunch Spots",
        icon = "lunch_dining",
        colorHex = "#FF9800",
        isSystem = true,
        sortOrder = 1
    )

    val LATE_NIGHT = Collection(
        id = "late_night",
        name = "Late Night",
        icon = "nightlife",
        colorHex = "#9C27B0",
        isSystem = true,
        sortOrder = 2
    )

    val VEGETARIAN = Collection(
        id = "vegetarian",
        name = "Vegetarian",
        icon = "eco",
        colorHex = "#4CAF50",
        isSystem = true,
        sortOrder = 3
    )

    val QUICK_BITES = Collection(
        id = "quick_bites",
        name = "Quick Bites",
        icon = "bolt",
        colorHex = "#2196F3",
        isSystem = true,
        sortOrder = 4
    )

    val ALL = listOf(FAVORITES, LUNCH, LATE_NIGHT, VEGETARIAN, QUICK_BITES)
}

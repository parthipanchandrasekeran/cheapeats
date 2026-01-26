package com.parthipan.cheapeats.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a restaurant in the Toronto food app.
 *
 * Indexed on:
 * - placeId: For quick lookups by Google Places ID
 * - priceLevel: For filtering by price
 * - nearTTC: For transit-accessible filtering
 */
@Entity(
    tableName = "restaurants",
    indices = [
        Index(value = ["place_id"], unique = true),
        Index(value = ["price_level"]),
        Index(value = ["near_ttc"]),
        Index(value = ["latitude", "longitude"])
    ]
)
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "place_id")
    val placeId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "cuisine")
    val cuisine: String,

    @ColumnInfo(name = "price_level")
    val priceLevel: Int, // 0-4 representing Free to $$$$

    @ColumnInfo(name = "rating")
    val rating: Float,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "is_sponsored")
    val isSponsored: Boolean = false,

    @ColumnInfo(name = "has_student_discount")
    val hasStudentDiscount: Boolean = false,

    @ColumnInfo(name = "near_ttc")
    val nearTTC: Boolean = false,

    @ColumnInfo(name = "average_price")
    val averagePrice: Float? = null,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    @ColumnInfo(name = "website")
    val website: String? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

package com.parthipan.cheapeats.data.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User settings stored in Room database.
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 0,
    val lowDataMode: Boolean = false,
    val cacheImagesOnWifi: Boolean = true,
    val maxCacheSizeMb: Int = 50,
    val prefetchNearby: Boolean = true
)

/**
 * Cache statistics for display in settings.
 */
data class CacheStats(
    val restaurantCount: Int = 0,
    val imageSizeBytes: Long = 0
) {
    val formattedSize: String
        get() = when {
            imageSizeBytes < 1024 -> "${imageSizeBytes}B"
            imageSizeBytes < 1024 * 1024 -> "${imageSizeBytes / 1024}KB"
            else -> "${imageSizeBytes / (1024 * 1024)}MB"
        }
}

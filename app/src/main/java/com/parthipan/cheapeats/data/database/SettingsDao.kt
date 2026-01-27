package com.parthipan.cheapeats.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parthipan.cheapeats.data.settings.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 0")
    fun getSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 0")
    suspend fun getSettingsSync(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)

    @Query("UPDATE user_settings SET lowDataMode = :enabled WHERE id = 0")
    suspend fun setLowDataMode(enabled: Boolean)

    @Query("UPDATE user_settings SET cacheImagesOnWifi = :enabled WHERE id = 0")
    suspend fun setCacheImagesOnWifi(enabled: Boolean)

    @Query("UPDATE user_settings SET prefetchNearby = :enabled WHERE id = 0")
    suspend fun setPrefetchNearby(enabled: Boolean)

    @Query("UPDATE user_settings SET maxCacheSizeMb = :sizeMb WHERE id = 0")
    suspend fun setMaxCacheSize(sizeMb: Int)
}

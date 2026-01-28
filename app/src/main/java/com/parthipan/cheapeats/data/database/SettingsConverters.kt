package com.parthipan.cheapeats.data.database

import androidx.room.TypeConverter
import com.parthipan.cheapeats.data.settings.ThemeMode

/**
 * Type converters for settings entities in Room database.
 */
class SettingsConverters {

    @TypeConverter
    fun fromThemeMode(mode: ThemeMode): String {
        return mode.name
    }

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode {
        return try {
            ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}

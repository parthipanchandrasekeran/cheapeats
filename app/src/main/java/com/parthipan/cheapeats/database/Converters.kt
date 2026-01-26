package com.parthipan.cheapeats.database

import androidx.room.TypeConverter

/**
 * Type converters for Room database.
 * Converts complex types to/from primitive types that SQLite can store.
 */
class Converters {

    // ============== DayOfWeek Converters ==============

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek): String {
        return day.name
    }

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek {
        return DayOfWeek.valueOf(value)
    }

    // ============== SpecialCategory Converters ==============

    @TypeConverter
    fun fromSpecialCategory(category: SpecialCategory): String {
        return category.name
    }

    @TypeConverter
    fun toSpecialCategory(value: String): SpecialCategory {
        return SpecialCategory.valueOf(value)
    }

    // ============== List<String> Converters (for future use) ==============

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|||")?.filter { it.isNotEmpty() }
    }
}

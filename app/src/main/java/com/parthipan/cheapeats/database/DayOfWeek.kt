package com.parthipan.cheapeats.database

import java.util.Calendar

/**
 * Enum representing days of the week for daily specials.
 * Values align with Calendar.DAY_OF_WEEK for easy conversion.
 */
enum class DayOfWeek(val displayName: String, val calendarValue: Int) {
    SUNDAY("Sunday", Calendar.SUNDAY),
    MONDAY("Monday", Calendar.MONDAY),
    TUESDAY("Tuesday", Calendar.TUESDAY),
    WEDNESDAY("Wednesday", Calendar.WEDNESDAY),
    THURSDAY("Thursday", Calendar.THURSDAY),
    FRIDAY("Friday", Calendar.FRIDAY),
    SATURDAY("Saturday", Calendar.SATURDAY);

    companion object {
        /**
         * Get the current day of week based on system time
         */
        fun today(): DayOfWeek {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            return fromCalendarValue(dayOfWeek)
        }

        /**
         * Convert Calendar.DAY_OF_WEEK value to DayOfWeek enum
         */
        fun fromCalendarValue(calendarValue: Int): DayOfWeek {
            return entries.find { it.calendarValue == calendarValue }
                ?: throw IllegalArgumentException("Invalid calendar day value: $calendarValue")
        }

        /**
         * Get DayOfWeek from string name (case-insensitive)
         */
        fun fromName(name: String): DayOfWeek? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}

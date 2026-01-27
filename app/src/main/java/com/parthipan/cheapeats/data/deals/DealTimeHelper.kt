package com.parthipan.cheapeats.data.deals

import java.util.Calendar

/**
 * Helper for time-based deal activation and expiry.
 */
object DealTimeHelper {

    // Day bitmask constants
    const val MONDAY = 1
    const val TUESDAY = 2
    const val WEDNESDAY = 4
    const val THURSDAY = 8
    const val FRIDAY = 16
    const val SATURDAY = 32
    const val SUNDAY = 64
    const val WEEKDAYS = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY
    const val WEEKENDS = SATURDAY or SUNDAY
    const val ALL_DAYS = WEEKDAYS or WEEKENDS

    /**
     * Get today's day as a bitmask value.
     */
    fun getTodayBitmask(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            Calendar.SATURDAY -> SATURDAY
            Calendar.SUNDAY -> SUNDAY
            else -> 0
        }
    }

    /**
     * Check if a deal is currently active based on all time constraints.
     */
    fun isDealActiveNow(deal: Deal): Boolean {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Check date validity
        if (deal.validFrom != null && now < deal.validFrom) return false
        if (deal.validUntil != null && now > deal.validUntil) return false

        // Check day of week
        val todayMask = getTodayBitmask()
        if (deal.validDays != 0 && deal.validDays != ALL_DAYS &&
            (deal.validDays and todayMask) == 0) {
            return false
        }

        // Check time of day
        if (deal.startTime != null && deal.endTime != null) {
            val currentTime = String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
            if (currentTime < deal.startTime || currentTime > deal.endTime) {
                return false
            }
        }

        return true
    }

    /**
     * Get human-readable time remaining text for a deal.
     * Returns null if deal has no time constraint or is expired.
     */
    fun getTimeRemainingText(deal: Deal): String? {
        if (deal.validUntil == null && deal.endTime == null) return null

        val now = System.currentTimeMillis()

        // Check if expiring today by end time
        if (deal.endTime != null && isDealActiveNow(deal)) {
            val endParts = deal.endTime.split(":")
            if (endParts.size == 2) {
                val endCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, endParts[0].toIntOrNull() ?: return null)
                    set(Calendar.MINUTE, endParts[1].toIntOrNull() ?: return null)
                    set(Calendar.SECOND, 0)
                }
                val minutesRemaining = (endCalendar.timeInMillis - now) / 60000

                return when {
                    minutesRemaining <= 0 -> null
                    minutesRemaining < 60 -> "Ends in ${minutesRemaining}min"
                    minutesRemaining < 120 -> "Ends in 1hr ${minutesRemaining - 60}min"
                    else -> "Until ${deal.endTime}"
                }
            }
        }

        // Check absolute expiry
        if (deal.validUntil != null) {
            val hoursRemaining = (deal.validUntil - now) / 3600000
            return when {
                hoursRemaining <= 0 -> null
                hoursRemaining < 24 -> "Ends in ${hoursRemaining}hr"
                hoursRemaining < 48 -> "Ends tomorrow"
                else -> null
            }
        }

        return null
    }

    /**
     * Get a human-readable description of valid days.
     */
    fun getValidDaysText(validDays: Int): String {
        return when (validDays) {
            ALL_DAYS, 0 -> "Every day"
            WEEKDAYS -> "Weekdays"
            WEEKENDS -> "Weekends"
            else -> {
                val days = mutableListOf<String>()
                if (validDays and MONDAY != 0) days.add("Mon")
                if (validDays and TUESDAY != 0) days.add("Tue")
                if (validDays and WEDNESDAY != 0) days.add("Wed")
                if (validDays and THURSDAY != 0) days.add("Thu")
                if (validDays and FRIDAY != 0) days.add("Fri")
                if (validDays and SATURDAY != 0) days.add("Sat")
                if (validDays and SUNDAY != 0) days.add("Sun")
                days.joinToString(", ")
            }
        }
    }

    /**
     * Create a bitmask from a list of day constants.
     */
    fun createDaysMask(vararg days: Int): Int {
        return days.fold(0) { acc, day -> acc or day }
    }
}

package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for the DayOfWeek enum.
 */
class DayOfWeekTest {

    // ==================== Calendar Value Tests ====================

    @Test
    fun `SUNDAY has correct calendar value`() {
        assertEquals(Calendar.SUNDAY, DayOfWeek.SUNDAY.calendarValue)
    }

    @Test
    fun `MONDAY has correct calendar value`() {
        assertEquals(Calendar.MONDAY, DayOfWeek.MONDAY.calendarValue)
    }

    @Test
    fun `TUESDAY has correct calendar value`() {
        assertEquals(Calendar.TUESDAY, DayOfWeek.TUESDAY.calendarValue)
    }

    @Test
    fun `WEDNESDAY has correct calendar value`() {
        assertEquals(Calendar.WEDNESDAY, DayOfWeek.WEDNESDAY.calendarValue)
    }

    @Test
    fun `THURSDAY has correct calendar value`() {
        assertEquals(Calendar.THURSDAY, DayOfWeek.THURSDAY.calendarValue)
    }

    @Test
    fun `FRIDAY has correct calendar value`() {
        assertEquals(Calendar.FRIDAY, DayOfWeek.FRIDAY.calendarValue)
    }

    @Test
    fun `SATURDAY has correct calendar value`() {
        assertEquals(Calendar.SATURDAY, DayOfWeek.SATURDAY.calendarValue)
    }

    // ==================== Display Name Tests ====================

    @Test
    fun `SUNDAY has correct display name`() {
        assertEquals("Sunday", DayOfWeek.SUNDAY.displayName)
    }

    @Test
    fun `MONDAY has correct display name`() {
        assertEquals("Monday", DayOfWeek.MONDAY.displayName)
    }

    @Test
    fun `TUESDAY has correct display name`() {
        assertEquals("Tuesday", DayOfWeek.TUESDAY.displayName)
    }

    @Test
    fun `WEDNESDAY has correct display name`() {
        assertEquals("Wednesday", DayOfWeek.WEDNESDAY.displayName)
    }

    @Test
    fun `THURSDAY has correct display name`() {
        assertEquals("Thursday", DayOfWeek.THURSDAY.displayName)
    }

    @Test
    fun `FRIDAY has correct display name`() {
        assertEquals("Friday", DayOfWeek.FRIDAY.displayName)
    }

    @Test
    fun `SATURDAY has correct display name`() {
        assertEquals("Saturday", DayOfWeek.SATURDAY.displayName)
    }

    // ==================== fromCalendarValue Tests ====================

    @Test
    fun `fromCalendarValue converts Sunday correctly`() {
        assertEquals(DayOfWeek.SUNDAY, DayOfWeek.fromCalendarValue(Calendar.SUNDAY))
    }

    @Test
    fun `fromCalendarValue converts Monday correctly`() {
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.fromCalendarValue(Calendar.MONDAY))
    }

    @Test
    fun `fromCalendarValue converts Tuesday correctly`() {
        assertEquals(DayOfWeek.TUESDAY, DayOfWeek.fromCalendarValue(Calendar.TUESDAY))
    }

    @Test
    fun `fromCalendarValue converts Wednesday correctly`() {
        assertEquals(DayOfWeek.WEDNESDAY, DayOfWeek.fromCalendarValue(Calendar.WEDNESDAY))
    }

    @Test
    fun `fromCalendarValue converts Thursday correctly`() {
        assertEquals(DayOfWeek.THURSDAY, DayOfWeek.fromCalendarValue(Calendar.THURSDAY))
    }

    @Test
    fun `fromCalendarValue converts Friday correctly`() {
        assertEquals(DayOfWeek.FRIDAY, DayOfWeek.fromCalendarValue(Calendar.FRIDAY))
    }

    @Test
    fun `fromCalendarValue converts Saturday correctly`() {
        assertEquals(DayOfWeek.SATURDAY, DayOfWeek.fromCalendarValue(Calendar.SATURDAY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromCalendarValue throws exception for invalid value 0`() {
        DayOfWeek.fromCalendarValue(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromCalendarValue throws exception for invalid value 8`() {
        DayOfWeek.fromCalendarValue(8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromCalendarValue throws exception for negative value`() {
        DayOfWeek.fromCalendarValue(-1)
    }

    // ==================== fromName Tests ====================

    @Test
    fun `fromName converts Sunday correctly`() {
        assertEquals(DayOfWeek.SUNDAY, DayOfWeek.fromName("SUNDAY"))
    }

    @Test
    fun `fromName converts Monday correctly`() {
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.fromName("MONDAY"))
    }

    @Test
    fun `fromName is case insensitive lowercase`() {
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.fromName("monday"))
    }

    @Test
    fun `fromName is case insensitive mixed case`() {
        assertEquals(DayOfWeek.WEDNESDAY, DayOfWeek.fromName("Wednesday"))
    }

    @Test
    fun `fromName returns null for invalid name`() {
        assertNull(DayOfWeek.fromName("NotADay"))
    }

    @Test
    fun `fromName returns null for empty string`() {
        assertNull(DayOfWeek.fromName(""))
    }

    @Test
    fun `fromName returns null for partial match`() {
        assertNull(DayOfWeek.fromName("Mon"))
    }

    // ==================== today Tests ====================

    @Test
    fun `today returns a valid DayOfWeek`() {
        val today = DayOfWeek.today()
        assertNotNull(today)
        assertTrue(DayOfWeek.entries.contains(today))
    }

    @Test
    fun `today matches Calendar today`() {
        val calendar = Calendar.getInstance()
        val calendarDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val today = DayOfWeek.today()

        assertEquals(calendarDayOfWeek, today.calendarValue)
    }

    // ==================== Enum Completeness Tests ====================

    @Test
    fun `DayOfWeek has exactly 7 entries`() {
        assertEquals(7, DayOfWeek.entries.size)
    }

    @Test
    fun `all DayOfWeek entries have unique calendar values`() {
        val calendarValues = DayOfWeek.entries.map { it.calendarValue }
        assertEquals(7, calendarValues.toSet().size)
    }

    @Test
    fun `all DayOfWeek entries have unique display names`() {
        val displayNames = DayOfWeek.entries.map { it.displayName }
        assertEquals(7, displayNames.toSet().size)
    }

    @Test
    fun `calendar values cover all days 1-7`() {
        val expectedValues = setOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )
        val actualValues = DayOfWeek.entries.map { it.calendarValue }.toSet()

        assertEquals(expectedValues, actualValues)
    }
}

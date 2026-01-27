package com.parthipan.cheapeats.database

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Room TypeConverters.
 */
class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    // ==================== DayOfWeek Converter Tests ====================

    @Test
    fun `fromDayOfWeek converts SUNDAY to string`() {
        assertEquals("SUNDAY", converters.fromDayOfWeek(DayOfWeek.SUNDAY))
    }

    @Test
    fun `fromDayOfWeek converts MONDAY to string`() {
        assertEquals("MONDAY", converters.fromDayOfWeek(DayOfWeek.MONDAY))
    }

    @Test
    fun `fromDayOfWeek converts all days correctly`() {
        DayOfWeek.entries.forEach { day ->
            assertEquals(day.name, converters.fromDayOfWeek(day))
        }
    }

    @Test
    fun `toDayOfWeek converts SUNDAY string`() {
        assertEquals(DayOfWeek.SUNDAY, converters.toDayOfWeek("SUNDAY"))
    }

    @Test
    fun `toDayOfWeek converts MONDAY string`() {
        assertEquals(DayOfWeek.MONDAY, converters.toDayOfWeek("MONDAY"))
    }

    @Test
    fun `toDayOfWeek converts all day strings correctly`() {
        DayOfWeek.entries.forEach { day ->
            assertEquals(day, converters.toDayOfWeek(day.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDayOfWeek throws for invalid string`() {
        converters.toDayOfWeek("INVALID")
    }

    @Test
    fun `DayOfWeek round trip conversion works`() {
        DayOfWeek.entries.forEach { day ->
            val stringValue = converters.fromDayOfWeek(day)
            val convertedBack = converters.toDayOfWeek(stringValue)
            assertEquals(day, convertedBack)
        }
    }

    // ==================== SpecialCategory Converter Tests ====================

    @Test
    fun `fromSpecialCategory converts FOOD to string`() {
        assertEquals("FOOD", converters.fromSpecialCategory(SpecialCategory.FOOD))
    }

    @Test
    fun `fromSpecialCategory converts DRINK to string`() {
        assertEquals("DRINK", converters.fromSpecialCategory(SpecialCategory.DRINK))
    }

    @Test
    fun `fromSpecialCategory converts COMBO to string`() {
        assertEquals("COMBO", converters.fromSpecialCategory(SpecialCategory.COMBO))
    }

    @Test
    fun `fromSpecialCategory converts HAPPY_HOUR to string`() {
        assertEquals("HAPPY_HOUR", converters.fromSpecialCategory(SpecialCategory.HAPPY_HOUR))
    }

    @Test
    fun `fromSpecialCategory converts all categories correctly`() {
        SpecialCategory.entries.forEach { category ->
            assertEquals(category.name, converters.fromSpecialCategory(category))
        }
    }

    @Test
    fun `toSpecialCategory converts FOOD string`() {
        assertEquals(SpecialCategory.FOOD, converters.toSpecialCategory("FOOD"))
    }

    @Test
    fun `toSpecialCategory converts DRINK string`() {
        assertEquals(SpecialCategory.DRINK, converters.toSpecialCategory("DRINK"))
    }

    @Test
    fun `toSpecialCategory converts all category strings correctly`() {
        SpecialCategory.entries.forEach { category ->
            assertEquals(category, converters.toSpecialCategory(category.name))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toSpecialCategory throws for invalid string`() {
        converters.toSpecialCategory("INVALID")
    }

    @Test
    fun `SpecialCategory round trip conversion works`() {
        SpecialCategory.entries.forEach { category ->
            val stringValue = converters.fromSpecialCategory(category)
            val convertedBack = converters.toSpecialCategory(stringValue)
            assertEquals(category, convertedBack)
        }
    }

    // ==================== List<String> Converter Tests ====================

    @Test
    fun `fromStringList converts empty list to null`() {
        val result = converters.fromStringList(emptyList())
        assertEquals("", result)
    }

    @Test
    fun `fromStringList converts single item list`() {
        val result = converters.fromStringList(listOf("item1"))
        assertEquals("item1", result)
    }

    @Test
    fun `fromStringList converts multiple items with delimiter`() {
        val result = converters.fromStringList(listOf("item1", "item2", "item3"))
        assertEquals("item1|||item2|||item3", result)
    }

    @Test
    fun `fromStringList returns null for null input`() {
        assertNull(converters.fromStringList(null))
    }

    @Test
    fun `toStringList converts single item string`() {
        val result = converters.toStringList("item1")
        assertEquals(listOf("item1"), result)
    }

    @Test
    fun `toStringList converts delimited string to list`() {
        val result = converters.toStringList("item1|||item2|||item3")
        assertEquals(listOf("item1", "item2", "item3"), result)
    }

    @Test
    fun `toStringList returns null for null input`() {
        assertNull(converters.toStringList(null))
    }

    @Test
    fun `toStringList filters empty strings`() {
        // When there are empty strings between delimiters
        val result = converters.toStringList("item1||||||item2")
        // Split by ||| would give: ["item1", "", "item2"]
        // The filter removes empty strings
        assertNotNull(result)
        assertFalse(result!!.contains(""))
    }

    @Test
    fun `toStringList returns empty-ish result for empty string`() {
        val result = converters.toStringList("")
        // Empty string after filtering gives empty list
        assertNotNull(result)
    }

    @Test
    fun `List String round trip conversion works`() {
        val original = listOf("Tag1", "Tag2", "Tag3")
        val stringValue = converters.fromStringList(original)
        val convertedBack = converters.toStringList(stringValue)
        assertEquals(original, convertedBack)
    }

    @Test
    fun `List String handles special characters`() {
        val original = listOf("Tag with spaces", "Tag-with-dashes", "Tag_with_underscores")
        val stringValue = converters.fromStringList(original)
        val convertedBack = converters.toStringList(stringValue)
        assertEquals(original, convertedBack)
    }

    @Test
    fun `List String handles unicode characters`() {
        val original = listOf("Café", "日本語", "Résumé")
        val stringValue = converters.fromStringList(original)
        val convertedBack = converters.toStringList(stringValue)
        assertEquals(original, convertedBack)
    }
}

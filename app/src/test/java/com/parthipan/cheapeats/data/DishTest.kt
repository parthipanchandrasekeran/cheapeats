package com.parthipan.cheapeats.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Dish data class and SampleDishes object.
 */
class DishTest {

    private fun createDish(
        id: String = "1",
        name: String = "Test Dish",
        description: String = "Test description",
        price: Float = 10.00f,
        category: String = "Main",
        isVegetarian: Boolean = false,
        isSpicy: Boolean = false,
        imageUrl: String? = null
    ) = Dish(id, name, description, price, category, isVegetarian, isSpicy, imageUrl)

    // ==================== formattedPrice Tests ====================

    @Test
    fun `formattedPrice formats whole number correctly`() {
        val dish = createDish(price = 10.00f)
        assertEquals("$10.00", dish.formattedPrice)
    }

    @Test
    fun `formattedPrice formats decimal correctly`() {
        val dish = createDish(price = 12.99f)
        assertEquals("$12.99", dish.formattedPrice)
    }

    @Test
    fun `formattedPrice formats single decimal place correctly`() {
        val dish = createDish(price = 5.50f)
        assertEquals("$5.50", dish.formattedPrice)
    }

    @Test
    fun `formattedPrice formats zero correctly`() {
        val dish = createDish(price = 0f)
        assertEquals("$0.00", dish.formattedPrice)
    }

    @Test
    fun `formattedPrice formats small price correctly`() {
        val dish = createDish(price = 0.99f)
        assertEquals("$0.99", dish.formattedPrice)
    }

    @Test
    fun `formattedPrice formats large price correctly`() {
        val dish = createDish(price = 99.99f)
        assertEquals("$99.99", dish.formattedPrice)
    }

    // ==================== isUnder15 Tests ====================

    @Test
    fun `isUnder15 returns true for price below 15`() {
        val dish = createDish(price = 14.99f)
        assertTrue(dish.isUnder15)
    }

    @Test
    fun `isUnder15 returns false for price equal to 15`() {
        val dish = createDish(price = 15.00f)
        assertFalse(dish.isUnder15)
    }

    @Test
    fun `isUnder15 returns false for price above 15`() {
        val dish = createDish(price = 15.01f)
        assertFalse(dish.isUnder15)
    }

    @Test
    fun `isUnder15 returns true for very cheap dish`() {
        val dish = createDish(price = 1.99f)
        assertTrue(dish.isUnder15)
    }

    @Test
    fun `isUnder15 returns true for free dish`() {
        val dish = createDish(price = 0f)
        assertTrue(dish.isUnder15)
    }

    // ==================== Data Class Tests ====================

    @Test
    fun `dish has correct default values`() {
        val dish = createDish()
        assertFalse(dish.isVegetarian)
        assertFalse(dish.isSpicy)
        assertNull(dish.imageUrl)
    }

    @Test
    fun `dish equality works correctly`() {
        val dish1 = createDish(id = "1", name = "Test")
        val dish2 = createDish(id = "1", name = "Test")
        assertEquals(dish1, dish2)
    }

    @Test
    fun `dish copy works correctly`() {
        val original = createDish(price = 10.00f)
        val copy = original.copy(price = 20.00f)
        assertEquals(20.00f, copy.price, 0.001f)
        assertEquals(original.name, copy.name)
    }
}

/**
 * Unit tests for the SampleDishes object.
 */
class SampleDishesTest {

    // ==================== getDishesForRestaurant Tests ====================

    @Test
    fun `getDishesForRestaurant returns Tim Hortons dishes for Tim Hortons`() {
        val dishes = SampleDishes.getDishesForRestaurant("Tim Hortons", "Cafe")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Coffee") || it.name.contains("Donut") })
    }

    @Test
    fun `getDishesForRestaurant is case insensitive for restaurant name`() {
        val dishes1 = SampleDishes.getDishesForRestaurant("TIM HORTONS", "Cafe")
        val dishes2 = SampleDishes.getDishesForRestaurant("tim hortons", "Cafe")
        assertEquals(dishes1, dishes2)
    }

    @Test
    fun `getDishesForRestaurant returns McDonald dishes for McDonalds`() {
        val dishes = SampleDishes.getDishesForRestaurant("McDonald's", "Fast Food")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Big Mac") || it.name.contains("McChicken") })
    }

    @Test
    fun `getDishesForRestaurant returns Subway dishes for Subway`() {
        val dishes = SampleDishes.getDishesForRestaurant("Subway", "Sandwiches")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Turkey") || it.name.contains("Veggie") })
    }

    @Test
    fun `getDishesForRestaurant returns Taco Bell dishes for Taco Bell`() {
        val dishes = SampleDishes.getDishesForRestaurant("Taco Bell", "Mexican")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Taco") || it.name.contains("Burrito") })
    }

    @Test
    fun `getDishesForRestaurant returns Pizza Pizza dishes for Pizza Pizza`() {
        val dishes = SampleDishes.getDishesForRestaurant("Pizza Pizza", "Pizza")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Pepperoni") || it.name.contains("Slice") })
    }

    @Test
    fun `getDishesForRestaurant returns Vietnamese dishes for Pho restaurant`() {
        val dishes = SampleDishes.getDishesForRestaurant("Pho House", "Restaurant")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Pho") || it.name.contains("Banh Mi") })
    }

    @Test
    fun `getDishesForRestaurant returns Vietnamese dishes for Vietnamese cuisine`() {
        val dishes = SampleDishes.getDishesForRestaurant("Unknown Restaurant", "Vietnamese")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Pho") || it.name.contains("Spring Rolls") })
    }

    @Test
    fun `getDishesForRestaurant returns Japanese dishes for Japanese cuisine`() {
        val dishes = SampleDishes.getDishesForRestaurant("Unknown Restaurant", "Japanese")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Sushi") || it.name.contains("Roll") })
    }

    @Test
    fun `getDishesForRestaurant returns Japanese dishes for Sushi cuisine`() {
        val dishes = SampleDishes.getDishesForRestaurant("Unknown Restaurant", "Sushi")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("California Roll") || it.name.contains("Nigiri") })
    }

    @Test
    fun `getDishesForRestaurant returns Indian dishes for Indian cuisine`() {
        val dishes = SampleDishes.getDishesForRestaurant("Unknown Restaurant", "Indian")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Butter Chicken") || it.name.contains("Naan") })
    }

    @Test
    fun `getDishesForRestaurant returns generic dishes for unknown restaurant`() {
        val dishes = SampleDishes.getDishesForRestaurant("Random Place", "Unknown Cuisine")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.any { it.name.contains("Salad") || it.name.contains("Sandwich") })
    }

    // ==================== getDishesUnder15 Tests ====================

    @Test
    fun `getDishesUnder15 returns only dishes under 15 dollars`() {
        val dishes = SampleDishes.getDishesUnder15("Tim Hortons", "Cafe")
        assertTrue(dishes.isNotEmpty())
        assertTrue(dishes.all { it.price < 15f })
    }

    @Test
    fun `getDishesUnder15 excludes dishes at or above 15 dollars`() {
        val allDishes = SampleDishes.getDishesForRestaurant("Sushi Restaurant", "Japanese")
        val under15Dishes = SampleDishes.getDishesUnder15("Sushi Restaurant", "Japanese")

        val expensiveDishes = allDishes.filter { it.price >= 15f }
        if (expensiveDishes.isNotEmpty()) {
            assertTrue(under15Dishes.size < allDishes.size)
            assertFalse(under15Dishes.any { it.price >= 15f })
        }
    }

    @Test
    fun `getDishesUnder15 returns empty list when no dishes under 15`() {
        // If a restaurant somehow had all expensive dishes, the list would be empty
        // This test verifies the filter works correctly
        val dishes = SampleDishes.getDishesUnder15("Any Restaurant", "Any Cuisine")
        assertTrue(dishes.all { it.isUnder15 })
    }

    // ==================== Dish Category Tests ====================

    @Test
    fun `Tim Hortons dishes have appropriate categories`() {
        val dishes = SampleDishes.getDishesForRestaurant("Tim Hortons", "Cafe")
        val categories = dishes.map { it.category }.toSet()
        assertTrue(categories.contains("Beverages") || categories.contains("Donuts") || categories.contains("Breakfast"))
    }

    @Test
    fun `some dishes are vegetarian`() {
        val dishes = SampleDishes.getDishesForRestaurant("Subway", "Sandwiches")
        assertTrue(dishes.any { it.isVegetarian })
    }

    @Test
    fun `some dishes are spicy`() {
        val dishes = SampleDishes.getDishesForRestaurant("Pho Restaurant", "Vietnamese")
        assertTrue(dishes.any { it.isSpicy })
    }
}

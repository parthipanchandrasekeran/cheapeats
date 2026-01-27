package com.parthipan.cheapeats.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CheapEatsDatabase and DAOs.
 */
@RunWith(AndroidJUnit4::class)
class CheapEatsDatabaseTest {

    private lateinit var database: CheapEatsDatabase
    private lateinit var restaurantDao: RestaurantDao
    private lateinit var dailySpecialDao: DailySpecialDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CheapEatsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        restaurantDao = database.restaurantDao()
        dailySpecialDao = database.dailySpecialDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ==================== RestaurantDao Insert Tests ====================

    @Test
    fun insertRestaurant_and_getById() = runBlocking {
        val restaurant = createTestRestaurant(id = 1, name = "Test Restaurant")
        restaurantDao.insert(restaurant)

        val retrieved = restaurantDao.getById(1)
        assertNotNull(retrieved)
        assertEquals("Test Restaurant", retrieved?.name)
    }

    @Test
    fun insertAll_insertsMultipleRestaurants() = runBlocking {
        val restaurants = listOf(
            createTestRestaurant(id = 1, name = "Restaurant 1"),
            createTestRestaurant(id = 2, name = "Restaurant 2"),
            createTestRestaurant(id = 3, name = "Restaurant 3")
        )
        restaurantDao.insertAll(restaurants)

        val count = restaurantDao.getRestaurantCount()
        assertEquals(3, count)
    }

    @Test
    fun insert_withConflict_replaces() = runBlocking {
        val restaurant1 = createTestRestaurant(id = 0, placeId = "place_1", name = "Original")
        val id = restaurantDao.insert(restaurant1)

        val restaurant2 = createTestRestaurant(id = id, placeId = "place_1", name = "Updated")
        restaurantDao.insert(restaurant2)

        val retrieved = restaurantDao.getById(id)
        assertEquals("Updated", retrieved?.name)
    }

    // ==================== RestaurantDao Query Tests ====================

    @Test
    fun getAllRestaurants_returnsFlowWithRestaurants() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1),
            createTestRestaurant(id = 2)
        ))

        val restaurants = restaurantDao.getAllRestaurants().first()
        assertEquals(2, restaurants.size)
    }

    @Test
    fun getByPlaceId_findsRestaurant() = runBlocking {
        val restaurant = createTestRestaurant(placeId = "unique_place_123")
        restaurantDao.insert(restaurant)

        val retrieved = restaurantDao.getByPlaceId("unique_place_123")
        assertNotNull(retrieved)
        assertEquals("unique_place_123", retrieved?.placeId)
    }

    @Test
    fun getFavoriteRestaurants_onlyReturnsFavorites() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, isFavorite = true),
            createTestRestaurant(id = 2, isFavorite = false),
            createTestRestaurant(id = 3, isFavorite = true)
        ))

        val favorites = restaurantDao.getFavoriteRestaurants().first()
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun getTransitAccessibleRestaurants_onlyReturnsNearTTC() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, nearTTC = true),
            createTestRestaurant(id = 2, nearTTC = false),
            createTestRestaurant(id = 3, nearTTC = true)
        ))

        val nearTTC = restaurantDao.getTransitAccessibleRestaurants().first()
        assertEquals(2, nearTTC.size)
        assertTrue(nearTTC.all { it.nearTTC })
    }

    @Test
    fun getRestaurantsByMaxPrice_filtersCorrectly() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, priceLevel = 1),
            createTestRestaurant(id = 2, priceLevel = 2),
            createTestRestaurant(id = 3, priceLevel = 3),
            createTestRestaurant(id = 4, priceLevel = 4)
        ))

        val budget = restaurantDao.getRestaurantsByMaxPrice(2).first()
        assertEquals(2, budget.size)
        assertTrue(budget.all { it.priceLevel <= 2 })
    }

    @Test
    fun getRestaurantsWithStudentDiscount_filtersCorrectly() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, hasStudentDiscount = true),
            createTestRestaurant(id = 2, hasStudentDiscount = false),
            createTestRestaurant(id = 3, hasStudentDiscount = true)
        ))

        val withDiscount = restaurantDao.getRestaurantsWithStudentDiscount().first()
        assertEquals(2, withDiscount.size)
    }

    // ==================== RestaurantDao Update Tests ====================

    @Test
    fun updateFavoriteStatus_updates() = runBlocking {
        val restaurant = createTestRestaurant(id = 0, isFavorite = false)
        val id = restaurantDao.insert(restaurant)

        restaurantDao.updateFavoriteStatus(id, true)

        val retrieved = restaurantDao.getById(id)
        assertTrue(retrieved?.isFavorite ?: false)
    }

    @Test
    fun update_updatesRestaurant() = runBlocking {
        val restaurant = createTestRestaurant(id = 0, name = "Original")
        val id = restaurantDao.insert(restaurant)

        val updated = restaurant.copy(id = id, name = "Updated")
        restaurantDao.update(updated)

        val retrieved = restaurantDao.getById(id)
        assertEquals("Updated", retrieved?.name)
    }

    // ==================== RestaurantDao Delete Tests ====================

    @Test
    fun delete_removesRestaurant() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val id = restaurantDao.insert(restaurant)

        restaurantDao.deleteById(id)

        val retrieved = restaurantDao.getById(id)
        assertNull(retrieved)
    }

    @Test
    fun deleteAll_removesAllRestaurants() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1),
            createTestRestaurant(id = 2),
            createTestRestaurant(id = 3)
        ))

        restaurantDao.deleteAll()

        val count = restaurantDao.getRestaurantCount()
        assertEquals(0, count)
    }

    // ==================== RestaurantDao Search Tests ====================

    @Test
    fun searchRestaurants_byName() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, name = "Taco Palace"),
            createTestRestaurant(id = 2, name = "Pasta House"),
            createTestRestaurant(id = 3, name = "Taco Bell")
        ))

        val results = restaurantDao.searchRestaurants("Taco").first()
        assertEquals(2, results.size)
    }

    @Test
    fun searchRestaurants_byCuisine() = runBlocking {
        restaurantDao.insertAll(listOf(
            createTestRestaurant(id = 1, cuisine = "Mexican"),
            createTestRestaurant(id = 2, cuisine = "Italian"),
            createTestRestaurant(id = 3, cuisine = "Mexican")
        ))

        val results = restaurantDao.searchRestaurants("Mexican").first()
        assertEquals(2, results.size)
    }

    // ==================== DailySpecialDao Tests ====================

    @Test
    fun insertSpecial_and_getById() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val restaurantId = restaurantDao.insert(restaurant)

        val special = createTestSpecial(restaurantId = restaurantId, specialName = "Test Special")
        val specialId = dailySpecialDao.insert(special)

        val retrieved = dailySpecialDao.getById(specialId)
        assertNotNull(retrieved)
        assertEquals("Test Special", retrieved?.specialName)
    }

    @Test
    fun getTodaysSpecials_filtersCorrectly() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val restaurantId = restaurantDao.insert(restaurant)

        val today = DayOfWeek.today()
        val notToday = DayOfWeek.entries.first { it != today }

        dailySpecialDao.insertAll(listOf(
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = today),
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = today),
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = notToday)
        ))

        val todaysSpecials = dailySpecialDao.getTodaysSpecials(today).first()
        assertEquals(2, todaysSpecials.size)
    }

    @Test
    fun getSpecialsForDay_filtersCorrectly() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val restaurantId = restaurantDao.insert(restaurant)

        dailySpecialDao.insertAll(listOf(
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = DayOfWeek.MONDAY),
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = DayOfWeek.MONDAY),
            createTestSpecial(restaurantId = restaurantId, dayOfWeek = DayOfWeek.TUESDAY)
        ))

        val mondaySpecials = dailySpecialDao.getSpecialsForDay(DayOfWeek.MONDAY).first()
        assertEquals(2, mondaySpecials.size)
    }

    @Test
    fun deleteSpecial_cascadesFromRestaurant() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val restaurantId = restaurantDao.insert(restaurant)

        dailySpecialDao.insert(createTestSpecial(restaurantId = restaurantId))
        dailySpecialDao.insert(createTestSpecial(restaurantId = restaurantId))

        restaurantDao.deleteById(restaurantId)

        val count = dailySpecialDao.getActiveSpecialsCount()
        assertEquals(0, count)
    }

    @Test
    fun updateActiveStatus_updates() = runBlocking {
        val restaurant = createTestRestaurant(id = 0)
        val restaurantId = restaurantDao.insert(restaurant)

        val special = createTestSpecial(restaurantId = restaurantId, isActive = true)
        val specialId = dailySpecialDao.insert(special)

        dailySpecialDao.updateActiveStatus(specialId, false)

        val retrieved = dailySpecialDao.getById(specialId)
        assertFalse(retrieved?.isActive ?: true)
    }

    // ==================== Relationship Tests ====================

    @Test
    fun getRestaurantWithSpecials_loadsRelationship() = runBlocking {
        val restaurant = createTestRestaurant(id = 0, name = "Test Restaurant")
        val restaurantId = restaurantDao.insert(restaurant)

        dailySpecialDao.insertAll(listOf(
            createTestSpecial(restaurantId = restaurantId, specialName = "Special 1"),
            createTestSpecial(restaurantId = restaurantId, specialName = "Special 2")
        ))

        val result = restaurantDao.getRestaurantWithSpecials(restaurantId)
        assertNotNull(result)
        assertEquals("Test Restaurant", result?.restaurant?.name)
        assertEquals(2, result?.specials?.size)
    }

    // ==================== Helper Functions ====================

    private fun createTestRestaurant(
        id: Long = 0,
        placeId: String = "place_${System.nanoTime()}",
        name: String = "Test Restaurant",
        cuisine: String = "Italian",
        priceLevel: Int = 2,
        nearTTC: Boolean = false,
        hasStudentDiscount: Boolean = false,
        isFavorite: Boolean = false
    ) = RestaurantEntity(
        id = id,
        placeId = placeId,
        name = name,
        cuisine = cuisine,
        priceLevel = priceLevel,
        rating = 4.5f,
        address = "123 Test St",
        latitude = 43.7615,
        longitude = -79.3456,
        nearTTC = nearTTC,
        hasStudentDiscount = hasStudentDiscount,
        isFavorite = isFavorite
    )

    private fun createTestSpecial(
        id: Long = 0,
        restaurantId: Long,
        dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        specialName: String = "Test Special",
        isActive: Boolean = true
    ) = DailySpecialEntity(
        id = id,
        restaurantId = restaurantId,
        dayOfWeek = dayOfWeek,
        specialName = specialName,
        description = "Test description",
        specialPrice = 10.00f,
        isActive = isActive
    )
}

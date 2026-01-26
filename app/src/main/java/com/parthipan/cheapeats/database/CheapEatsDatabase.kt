package com.parthipan.cheapeats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database for the CheapEats Toronto food app.
 *
 * Contains:
 * - RestaurantEntity: Stores restaurant information
 * - DailySpecialEntity: Stores daily specials with day-of-week indexing
 *
 * Features:
 * - Efficient "today's specials" queries via indexed day_of_week column
 * - One-to-many relationship between Restaurant and DailySpecial
 * - Type converters for DayOfWeek and SpecialCategory enums
 */
@Database(
    entities = [
        RestaurantEntity::class,
        DailySpecialEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CheapEatsDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao
    abstract fun dailySpecialDao(): DailySpecialDao

    companion object {
        private const val DATABASE_NAME = "cheapeats_database"

        @Volatile
        private var INSTANCE: CheapEatsDatabase? = null

        /**
         * Get the singleton database instance.
         *
         * @param context Application context
         * @param scope Optional CoroutineScope for pre-populating data
         * @return The database instance
         */
        fun getDatabase(
            context: Context,
            scope: CoroutineScope? = null
        ): CheapEatsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheapEatsDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration() // For development; use migrations in production
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Get database instance without pre-population (for testing).
         */
        fun getInMemoryDatabase(context: Context): CheapEatsDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                CheapEatsDatabase::class.java
            )
                .allowMainThreadQueries() // Only for testing
                .build()
        }
    }

    /**
     * Database callback for pre-populating sample data on first creation.
     */
    private class DatabaseCallback(
        private val scope: CoroutineScope?
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate with sample data when database is first created
            INSTANCE?.let { database ->
                scope?.launch(Dispatchers.IO) {
                    populateSampleData(database)
                }
            }
        }

        /**
         * Populate database with sample Toronto restaurants and specials.
         */
        private suspend fun populateSampleData(database: CheapEatsDatabase) {
            val restaurantDao = database.restaurantDao()
            val specialDao = database.dailySpecialDao()

            // Sample restaurants
            val restaurants = listOf(
                RestaurantEntity(
                    placeId = "sample_1",
                    name = "Kensington Kitchen",
                    cuisine = "Canadian",
                    priceLevel = 2,
                    rating = 4.5f,
                    address = "123 Augusta Ave, Toronto",
                    latitude = 43.6547,
                    longitude = -79.4006,
                    nearTTC = true,
                    hasStudentDiscount = true,
                    averagePrice = 18f
                ),
                RestaurantEntity(
                    placeId = "sample_2",
                    name = "Queen West Tacos",
                    cuisine = "Mexican",
                    priceLevel = 1,
                    rating = 4.3f,
                    address = "456 Queen St W, Toronto",
                    latitude = 43.6471,
                    longitude = -79.4042,
                    nearTTC = true,
                    hasStudentDiscount = true,
                    averagePrice = 12f
                ),
                RestaurantEntity(
                    placeId = "sample_3",
                    name = "Yonge Sushi",
                    cuisine = "Japanese",
                    priceLevel = 2,
                    rating = 4.7f,
                    address = "789 Yonge St, Toronto",
                    latitude = 43.6677,
                    longitude = -79.3857,
                    nearTTC = true,
                    hasStudentDiscount = false,
                    averagePrice = 25f
                )
            )

            val restaurantIds = restaurantDao.insertAll(restaurants)

            // Sample daily specials
            val specials = listOf(
                // Kensington Kitchen specials
                DailySpecialEntity(
                    restaurantId = restaurantIds[0],
                    dayOfWeek = DayOfWeek.MONDAY,
                    specialName = "Meatless Monday",
                    description = "All vegetarian dishes 20% off",
                    specialPrice = 14.99f,
                    originalPrice = 18.99f,
                    category = SpecialCategory.FOOD
                ),
                DailySpecialEntity(
                    restaurantId = restaurantIds[0],
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    specialName = "Wing Wednesday",
                    description = "Half-price wings all day",
                    specialPrice = 8.99f,
                    originalPrice = 17.99f,
                    category = SpecialCategory.FOOD
                ),
                // Queen West Tacos specials
                DailySpecialEntity(
                    restaurantId = restaurantIds[1],
                    dayOfWeek = DayOfWeek.TUESDAY,
                    specialName = "Taco Tuesday",
                    description = "All tacos $2 each, minimum 3",
                    specialPrice = 6.00f,
                    originalPrice = 12.00f,
                    category = SpecialCategory.FOOD,
                    termsConditions = "Dine-in only, minimum 3 tacos"
                ),
                DailySpecialEntity(
                    restaurantId = restaurantIds[1],
                    dayOfWeek = DayOfWeek.THURSDAY,
                    specialName = "Thirsty Thursday",
                    description = "$5 margaritas with any food purchase",
                    specialPrice = 5.00f,
                    originalPrice = 12.00f,
                    category = SpecialCategory.HAPPY_HOUR,
                    startTime = "16:00",
                    endTime = "20:00"
                ),
                // Yonge Sushi specials
                DailySpecialEntity(
                    restaurantId = restaurantIds[2],
                    dayOfWeek = DayOfWeek.FRIDAY,
                    specialName = "Sashimi Friday",
                    description = "Deluxe sashimi platter for 2",
                    specialPrice = 39.99f,
                    originalPrice = 55.99f,
                    category = SpecialCategory.DINNER
                ),
                DailySpecialEntity(
                    restaurantId = restaurantIds[2],
                    dayOfWeek = DayOfWeek.SATURDAY,
                    specialName = "Weekend Brunch Roll",
                    description = "Build your own roll + miso soup",
                    specialPrice = 15.99f,
                    originalPrice = 22.99f,
                    category = SpecialCategory.BRUNCH,
                    startTime = "10:00",
                    endTime = "14:00"
                ),
                DailySpecialEntity(
                    restaurantId = restaurantIds[2],
                    dayOfWeek = DayOfWeek.SUNDAY,
                    specialName = "Sunday Family Combo",
                    description = "Sushi boat for 4 with sides",
                    specialPrice = 69.99f,
                    originalPrice = 89.99f,
                    category = SpecialCategory.COMBO
                )
            )

            specialDao.insertAll(specials)
        }
    }
}

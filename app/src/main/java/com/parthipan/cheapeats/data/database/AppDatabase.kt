package com.parthipan.cheapeats.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.parthipan.cheapeats.data.cache.CachedRestaurant
import com.parthipan.cheapeats.data.deals.Deal
import com.parthipan.cheapeats.data.favorites.Collection
import com.parthipan.cheapeats.data.favorites.CollectionRestaurant
import com.parthipan.cheapeats.data.favorites.SystemCollections
import com.parthipan.cheapeats.data.favorites.ViewHistoryEntry
import com.parthipan.cheapeats.data.settings.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CachedRestaurant::class,
        Collection::class,
        CollectionRestaurant::class,
        ViewHistoryEntry::class,
        Deal::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cacheDao(): CacheDao
    abstract fun collectionDao(): CollectionDao
    abstract fun viewHistoryDao(): ViewHistoryDao
    abstract fun dealDao(): DealDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val DATABASE_NAME = "cheapeats_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback to initialize database with default data.
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        /**
         * Populate database with initial data.
         */
        private suspend fun populateDatabase(database: AppDatabase) {
            // Insert system collections
            database.collectionDao().insertCollections(SystemCollections.ALL)

            // Insert default settings
            database.settingsDao().saveSettings(UserSettings())
        }
    }
}

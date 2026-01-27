package com.parthipan.cheapeats.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parthipan.cheapeats.data.favorites.Collection
import com.parthipan.cheapeats.data.favorites.CollectionRestaurant
import com.parthipan.cheapeats.data.favorites.CollectionWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY sortOrder")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollection(id: String): Collection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<Collection>)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :id AND isSystem = 0")
    suspend fun deleteCollectionById(id: String)

    // Get restaurant IDs in a collection
    @Query("""
        SELECT restaurantId FROM collection_restaurants
        WHERE collectionId = :collectionId
        ORDER BY addedAt DESC
    """)
    fun getRestaurantIdsInCollection(collectionId: String): Flow<List<String>>

    // Get all collections containing a restaurant
    @Query("""
        SELECT c.* FROM collections c
        INNER JOIN collection_restaurants cr ON c.id = cr.collectionId
        WHERE cr.restaurantId = :restaurantId
    """)
    fun getCollectionsForRestaurant(restaurantId: String): Flow<List<Collection>>

    // Add restaurant to collection
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCollection(item: CollectionRestaurant)

    // Remove restaurant from collection
    @Query("""
        DELETE FROM collection_restaurants
        WHERE collectionId = :collectionId AND restaurantId = :restaurantId
    """)
    suspend fun removeFromCollection(collectionId: String, restaurantId: String)

    // Check if restaurant is in a specific collection
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM collection_restaurants
            WHERE collectionId = :collectionId AND restaurantId = :restaurantId
        )
    """)
    suspend fun isInCollection(collectionId: String, restaurantId: String): Boolean

    // Check if restaurant is in any collection
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM collection_restaurants
            WHERE restaurantId = :restaurantId
        )
    """)
    fun isInAnyCollection(restaurantId: String): Flow<Boolean>

    // Get collection counts
    @Query("""
        SELECT c.id, c.name, c.icon, c.colorHex, c.isSystem, c.sortOrder, c.createdAt,
               COUNT(cr.restaurantId) as restaurantCount
        FROM collections c
        LEFT JOIN collection_restaurants cr ON c.id = cr.collectionId
        GROUP BY c.id
        ORDER BY c.sortOrder
    """)
    fun getCollectionsWithCounts(): Flow<List<CollectionWithCount>>

    // Get count for a specific collection
    @Query("""
        SELECT COUNT(*) FROM collection_restaurants
        WHERE collectionId = :collectionId
    """)
    suspend fun getCollectionCount(collectionId: String): Int

    // Check if in favorites (shortcut for common use case)
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM collection_restaurants
            WHERE collectionId = 'favorites' AND restaurantId = :restaurantId
        )
    """)
    fun isInFavorites(restaurantId: String): Flow<Boolean>

    // Toggle favorite (convenience method implementation in repository)
    @Query("SELECT * FROM collection_restaurants WHERE collectionId = :collectionId AND restaurantId = :restaurantId")
    suspend fun getCollectionRestaurant(collectionId: String, restaurantId: String): CollectionRestaurant?
}

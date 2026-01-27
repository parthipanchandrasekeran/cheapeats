package com.parthipan.cheapeats.data.favorites

import com.parthipan.cheapeats.data.database.CollectionDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing collections and favorites.
 */
class CollectionRepository(
    private val collectionDao: CollectionDao
) {
    /**
     * Get all collections with their restaurant counts.
     */
    fun getCollectionsWithCounts(): Flow<List<CollectionWithCount>> {
        return collectionDao.getCollectionsWithCounts()
    }

    /**
     * Get all collections (without counts).
     */
    fun getAllCollections(): Flow<List<Collection>> {
        return collectionDao.getAllCollections()
    }

    /**
     * Get restaurant IDs in a specific collection.
     */
    fun getRestaurantIdsInCollection(collectionId: String): Flow<List<String>> {
        return collectionDao.getRestaurantIdsInCollection(collectionId)
    }

    /**
     * Get collections that contain a specific restaurant.
     */
    fun getCollectionsForRestaurant(restaurantId: String): Flow<List<Collection>> {
        return collectionDao.getCollectionsForRestaurant(restaurantId)
    }

    /**
     * Check if a restaurant is in the favorites collection.
     */
    fun isInFavorites(restaurantId: String): Flow<Boolean> {
        return collectionDao.isInFavorites(restaurantId)
    }

    /**
     * Check if a restaurant is in any collection.
     */
    fun isInAnyCollection(restaurantId: String): Flow<Boolean> {
        return collectionDao.isInAnyCollection(restaurantId)
    }

    /**
     * Add a restaurant to a collection.
     */
    suspend fun addToCollection(collectionId: String, restaurantId: String, note: String? = null) {
        collectionDao.addToCollection(
            CollectionRestaurant(
                collectionId = collectionId,
                restaurantId = restaurantId,
                note = note
            )
        )
    }

    /**
     * Remove a restaurant from a collection.
     */
    suspend fun removeFromCollection(collectionId: String, restaurantId: String) {
        collectionDao.removeFromCollection(collectionId, restaurantId)
    }

    /**
     * Toggle a restaurant's membership in a collection.
     */
    suspend fun toggleCollection(collectionId: String, restaurantId: String): Boolean {
        val isInCollection = collectionDao.isInCollection(collectionId, restaurantId)
        if (isInCollection) {
            removeFromCollection(collectionId, restaurantId)
            return false
        } else {
            addToCollection(collectionId, restaurantId)
            return true
        }
    }

    /**
     * Toggle favorite status (shortcut for favorites collection).
     */
    suspend fun toggleFavorite(restaurantId: String): Boolean {
        return toggleCollection("favorites", restaurantId)
    }

    /**
     * Create a new custom collection.
     */
    suspend fun createCollection(name: String, icon: String = "folder", colorHex: String = "#607D8B"): Collection {
        val collection = Collection(
            name = name,
            icon = icon,
            colorHex = colorHex,
            isSystem = false,
            sortOrder = 100  // Custom collections go after system ones
        )
        collectionDao.insertCollection(collection)
        return collection
    }

    /**
     * Delete a custom collection (system collections cannot be deleted).
     */
    suspend fun deleteCollection(collectionId: String) {
        collectionDao.deleteCollectionById(collectionId)
    }

    /**
     * Get the count of restaurants in a collection.
     */
    suspend fun getCollectionCount(collectionId: String): Int {
        return collectionDao.getCollectionCount(collectionId)
    }
}

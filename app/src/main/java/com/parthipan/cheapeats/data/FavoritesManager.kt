package com.parthipan.cheapeats.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user's favorite restaurants using SharedPreferences.
 * Favorites indicate trust and habit - used to suggest reliable, repeat choices.
 */
class FavoritesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "cheapeats_favorites"
        private const val KEY_FAVORITES = "favorite_ids"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(loadFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private fun loadFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    private fun saveFavorites(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, ids).apply()
        _favoriteIds.value = ids
    }

    fun isFavorite(restaurantId: String): Boolean {
        return restaurantId in _favoriteIds.value
    }

    fun toggleFavorite(restaurantId: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (restaurantId in current) {
            current.remove(restaurantId)
        } else {
            current.add(restaurantId)
        }
        saveFavorites(current)
    }

    fun addFavorite(restaurantId: String) {
        val current = _favoriteIds.value.toMutableSet()
        current.add(restaurantId)
        saveFavorites(current)
    }

    fun removeFavorite(restaurantId: String) {
        val current = _favoriteIds.value.toMutableSet()
        current.remove(restaurantId)
        saveFavorites(current)
    }

    fun getFavoriteCount(): Int = _favoriteIds.value.size

    fun hasFavorites(): Boolean = _favoriteIds.value.isNotEmpty()

    /**
     * Apply favorite status to a list of restaurants.
     */
    fun applyFavorites(restaurants: List<Restaurant>): List<Restaurant> {
        val favorites = _favoriteIds.value
        return restaurants.map { restaurant ->
            restaurant.copy(isFavorite = restaurant.id in favorites)
        }
    }
}

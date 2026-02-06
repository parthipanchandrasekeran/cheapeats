package com.parthipan.cheapeats.data.cache

import android.content.Context
import android.util.Log
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.database.CacheDao
import com.parthipan.cheapeats.data.settings.CacheStats
import com.parthipan.cheapeats.ui.filter.FilterState
import com.parthipan.cheapeats.ui.filter.FilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Manages offline caching and low-data mode functionality.
 */
class OfflineManager(
    private val context: Context,
    private val cacheDao: CacheDao
) {
    companion object {
        private const val TAG = "OfflineManager"
        const val MAX_CACHE_AGE_DAYS = 7
        const val MAX_CACHE_SIZE_MB = 50
        const val MAX_CACHED_RESTAURANTS = 200
        const val THUMBNAIL_SIZE = 200
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()

    // Store callback reference for cleanup
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        // Check initial connectivity state
        _isOffline.value = !isNetworkAvailable()

        // Monitor connectivity changes
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOffline.value = false
            }
            override fun onLost(network: Network) {
                _isOffline.value = !isNetworkAvailable()
            }
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network callback", e)
            networkCallback = null
        }
    }

    /**
     * Release resources. Call this when the manager is no longer needed.
     */
    fun release() {
        networkCallback?.let { callback ->
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to unregister network callback", e)
            }
            networkCallback = null
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isOnWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Cache restaurants from a successful API response.
     */
    suspend fun cacheResults(
        restaurants: List<Restaurant>,
        userLocation: LatLng?
    ) {
        val cached = restaurants.map { it.toCached(userLocation) }
        cacheDao.cacheRestaurants(cached)

        // Cache thumbnails in background (on wifi only by default)
        if (isOnWifi()) {
            restaurants.forEach { restaurant ->
                restaurant.imageUrl?.let { url ->
                    cacheThumbnail(restaurant.id, url)
                }
            }
        }

        updateStats()
    }

    /**
     * Get cached results when offline or as fallback.
     */
    suspend fun getCachedResults(
        userLocation: LatLng?,
        filterState: FilterState
    ): List<Restaurant> {
        val cached = if (userLocation != null) {
            cacheDao.getNearby(
                userLocation.latitude,
                userLocation.longitude
            ).first()
        } else {
            cacheDao.getRecentlyViewed().first()
        }

        return cached
            .map { it.toRestaurant() }
            .let { restaurants ->
                FilterViewModel.applyFilters(restaurants, filterState)
            }
    }

    /**
     * Record that user viewed a restaurant (for relevance).
     */
    suspend fun recordAccess(restaurantId: String) {
        cacheDao.touchRestaurant(restaurantId)
    }

    /**
     * Cache thumbnail image locally.
     */
    private suspend fun cacheThumbnail(restaurantId: String, imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(THUMBNAIL_SIZE)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@withContext

                    val thumbnailDir = File(context.cacheDir, "thumbnails")
                    thumbnailDir.mkdirs()
                    val file = File(thumbnailDir, "$restaurantId.jpg")

                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }

                    // Update cache entry with local path
                    cacheDao.updateThumbnailPath(restaurantId, file.absolutePath)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cache thumbnail for $restaurantId", e)
            }
        }
    }

    /**
     * Cleanup old cached data.
     */
    suspend fun cleanupOldData() {
        val weekAgo = System.currentTimeMillis() - (MAX_CACHE_AGE_DAYS * 24 * 3600000L)
        cacheDao.cleanupOldCache(weekAgo)

        // Clean up orphaned thumbnail files
        withContext(Dispatchers.IO) {
            try {
                val cachedIds = cacheDao.getRecentlyViewed(Int.MAX_VALUE).first().map { it.id }.toSet()
                val thumbnailDir = File(context.cacheDir, "thumbnails")
                thumbnailDir.listFiles()?.forEach { file ->
                    val id = file.nameWithoutExtension
                    if (id !in cachedIds) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean up orphaned thumbnails", e)
            }
        }

        updateStats()
    }

    /**
     * Clear all cached data.
     */
    suspend fun clearCache() {
        cacheDao.clearAllCache()

        // Delete all thumbnail files
        withContext(Dispatchers.IO) {
            File(context.cacheDir, "thumbnails").deleteRecursively()
        }

        updateStats()
    }

    private suspend fun updateStats() {
        _cacheStats.value = CacheStats(
            restaurantCount = cacheDao.getCacheCount(),
            imageSizeBytes = cacheDao.getCachedImageSize() ?: 0
        )
    }

    /**
     * Get current cache statistics.
     */
    suspend fun refreshStats(): CacheStats {
        updateStats()
        return _cacheStats.value
    }
}

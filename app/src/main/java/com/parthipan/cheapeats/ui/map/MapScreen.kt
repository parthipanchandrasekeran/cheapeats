package com.parthipan.cheapeats.ui.map

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.parthipan.cheapeats.data.Restaurant
import kotlinx.coroutines.launch

/**
 * ClusterItem wrapper for Restaurant data
 */
class RestaurantClusterItem(
    val restaurant: Restaurant
) : ClusterItem {
    override fun getPosition(): LatLng = restaurant.location
    override fun getTitle(): String = restaurant.name
    override fun getSnippet(): String = "${restaurant.cuisine} • ${restaurant.pricePoint} • ${restaurant.rating}"
    override fun getZIndex(): Float = if (restaurant.isSponsored) 1f else 0f
}

/**
 * Custom cluster renderer that shows gold markers for sponsored restaurants
 * and red markers for standard restaurants.
 * Uses icon caching for better performance.
 */
class RestaurantClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<RestaurantClusterItem>
) : DefaultClusterRenderer<RestaurantClusterItem>(context, map, clusterManager) {

    companion object {
        // Standard marker color (red)
        private const val STANDARD_HUE = BitmapDescriptorFactory.HUE_RED
        // Sponsored marker color (gold/yellow)
        private const val SPONSORED_HUE = BitmapDescriptorFactory.HUE_YELLOW

        // Cache for marker icons to avoid recreation
        private val standardMarker: BitmapDescriptor by lazy {
            BitmapDescriptorFactory.defaultMarker(STANDARD_HUE)
        }
        private val sponsoredMarker: BitmapDescriptor by lazy {
            BitmapDescriptorFactory.defaultMarker(SPONSORED_HUE)
        }

        // Cache for cluster icons (size -> icon)
        private val clusterIconCache = mutableMapOf<String, BitmapDescriptor>()
        private const val CLUSTER_ICON_DIAMETER = 80 // Smaller for faster rendering

        private fun getClusterIcon(size: Int, hasSponsored: Boolean): BitmapDescriptor {
            val key = "${size}_$hasSponsored"
            return clusterIconCache.getOrPut(key) {
                createClusterIconBitmap(size, hasSponsored)
            }
        }

        private fun createClusterIconBitmap(size: Int, hasSponsored: Boolean): BitmapDescriptor {
            val diameter = CLUSTER_ICON_DIAMETER
            val bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint().apply {
                isAntiAlias = true
                color = if (hasSponsored) {
                    Color.parseColor("#FFD700")
                } else {
                    Color.parseColor("#FF4444")
                }
                style = Paint.Style.FILL
            }
            canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint)

            paint.apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f - 2f, paint)

            paint.apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                textSize = 32f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val text = if (size > 99) "99+" else size.toString()
            val textY = diameter / 2f - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(text, diameter / 2f, textY, paint)

            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onBeforeClusterItemRendered(
        item: RestaurantClusterItem,
        markerOptions: MarkerOptions
    ) {
        // Use cached markers
        markerOptions.icon(if (item.restaurant.isSponsored) sponsoredMarker else standardMarker)
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<RestaurantClusterItem>,
        markerOptions: MarkerOptions
    ) {
        val hasSponsored = cluster.items.any { it.restaurant.isSponsored }
        markerOptions.icon(getClusterIcon(cluster.size, hasSponsored))
    }
}

@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    restaurants: List<Restaurant>,
    userLocation: LatLng? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var clusterManager by remember { mutableStateOf<ClusterManager<RestaurantClusterItem>?>(null) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Default to Toronto if no location provided
    val defaultLocation = LatLng(43.7615, -79.3456)
    val mapCenter = userLocation ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, 14f)
    }

    // Request permissions when screen loads
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Center map on user location when it changes
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }

    // Update cluster items when restaurants change
    LaunchedEffect(restaurants, clusterManager) {
        clusterManager?.let { manager ->
            manager.clearItems()
            restaurants.forEach { restaurant ->
                manager.addItem(RestaurantClusterItem(restaurant))
            }
            manager.cluster()
        }
    }

    val mapProperties = remember(locationPermissions.allPermissionsGranted) {
        MapProperties(
            isMyLocationEnabled = locationPermissions.allPermissionsGranted,
            isBuildingEnabled = false, // Disable 3D buildings for performance
            isIndoorEnabled = false    // Disable indoor maps for performance
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,     // Show zoom buttons
            mapToolbarEnabled = false,      // Disable toolbar for faster rendering
            myLocationButtonEnabled = true,
            compassEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = false     // Disable tilt for performance
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // Set up clustering using MapEffect
            MapEffect(restaurants) { map ->
                if (clusterManager == null) {
                    val manager = ClusterManager<RestaurantClusterItem>(context, map)
                    manager.renderer = RestaurantClusterRenderer(context, map, manager)

                    map.setOnCameraIdleListener(manager)
                    map.setOnMarkerClickListener(manager)

                    // Add click listener for cluster items
                    manager.setOnClusterItemClickListener { item ->
                        // Show info window on click
                        false // Return false to show default info window
                    }

                    // Add click listener for clusters
                    manager.setOnClusterClickListener { cluster ->
                        // Zoom in when cluster is clicked
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    cluster.position,
                                    cameraPositionState.position.zoom + 2f
                                )
                            )
                        }
                        true
                    }

                    clusterManager = manager
                }
            }
        }

        // Legend overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(12.dp)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFFFD700),
                                CircleShape
                            )
                    )
                    Text(
                        text = "Sponsored",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(12.dp)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFFF4444),
                                CircleShape
                            )
                    )
                    Text(
                        text = "Standard",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Info overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = if (userLocation != null) {
                    val sponsoredCount = restaurants.count { it.isSponsored }
                    "${restaurants.size} restaurants ($sponsoredCount sponsored)"
                } else if (!locationPermissions.allPermissionsGranted) {
                    "Enable location for nearby places"
                } else {
                    "Getting your location..."
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


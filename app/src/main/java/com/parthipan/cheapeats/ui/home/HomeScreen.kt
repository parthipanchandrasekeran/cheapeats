package com.parthipan.cheapeats.ui.home

import android.app.Activity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.parthipan.cheapeats.data.BillingService
import com.parthipan.cheapeats.data.FavoritesManager
import com.parthipan.cheapeats.data.PlacesService
import com.parthipan.cheapeats.data.PurchaseState
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.TipProduct
import com.parthipan.cheapeats.data.TransitHelper
import com.parthipan.cheapeats.data.VertexAiService
import com.parthipan.cheapeats.data.sampleRestaurants
import com.parthipan.cheapeats.ui.components.ShimmerRestaurantList
import com.parthipan.cheapeats.ui.detail.RestaurantDetailScreen
import com.parthipan.cheapeats.ui.filter.FilterBar
import com.parthipan.cheapeats.ui.filter.FilterViewModel
import com.parthipan.cheapeats.ui.lunchroute.LunchRouteBottomSheet
import com.parthipan.cheapeats.ui.lunchroute.LunchRouteViewModel
import com.parthipan.cheapeats.ui.map.MapScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Settings
import com.parthipan.cheapeats.data.database.AppDatabase
import com.parthipan.cheapeats.data.lunchroute.RouteStart
import com.parthipan.cheapeats.data.settings.CacheStats
import com.parthipan.cheapeats.data.settings.UserSettings
import com.parthipan.cheapeats.ui.settings.SettingsBottomSheet
import com.parthipan.cheapeats.data.favorites.ViewHistoryEntry
import com.parthipan.cheapeats.data.favorites.ViewSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class ViewMode {
    LIST, MAP
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    vertexAiService: VertexAiService? = null,
    placesService: PlacesService? = null,
    database: AppDatabase? = null,
    filterViewModel: FilterViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // First-time user tip
    var showFirstTimeTip by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("cheapeats_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            showFirstTimeTip = true
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoadingRestaurants by remember { mutableStateOf(true) }
    // Show sample data immediately while real data loads
    var restaurants by remember { mutableStateOf(sampleRestaurants) }
    var searchFilteredRestaurants by remember { mutableStateOf(sampleRestaurants) }
    var aiRecommendation by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isInTorontoArea by remember { mutableStateOf(true) } // Default to true, update when location known

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Navigation state for restaurant detail
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    // Menu and tip dialog state
    var showMenu by remember { mutableStateOf(false) }
    var showTipDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Settings state
    val userSettings by database?.settingsDao()?.getSettings()?.collectAsState(initial = UserSettings())
        ?: remember { mutableStateOf(UserSettings()) }
    val cacheStats = remember { CacheStats() } // Simplified for now

    // Lunch Route state
    var showLunchRoute by remember { mutableStateOf(false) }
    val lunchRouteViewModel: LunchRouteViewModel = viewModel()
    val lunchRouteState by lunchRouteViewModel.state.collectAsState()

    // Billing service
    val billingService = remember { BillingService(context) }
    val tipProducts by billingService.tipProducts.collectAsState()
    val purchaseState by billingService.purchaseState.collectAsState()

    // Favorites manager
    val favoritesManager = remember { FavoritesManager(context) }
    val favoriteIds by favoritesManager.favoriteIds.collectAsState()

    // Trending restaurants
    var trendingRestaurants by remember { mutableStateOf<List<Pair<Restaurant, Int>>>(emptyList()) }

    // Initialize billing on start
    DisposableEffect(Unit) {
        billingService.initialize()
        onDispose {
            billingService.disconnect()
        }
    }

    // Handle purchase state changes
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                snackbarHostState.showSnackbar("Thank you for your support!")
                showTipDialog = false
                billingService.resetPurchaseState()
            }
            is PurchaseState.Cancelled -> {
                billingService.resetPurchaseState()
            }
            is PurchaseState.Error -> {
                snackbarHostState.showSnackbar("Purchase failed. Please try again.")
                billingService.resetPurchaseState()
            }
            else -> {}
        }
    }


    // Load trending restaurants
    LaunchedEffect(restaurants) {
        if (database != null && restaurants.isNotEmpty()) {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val mostViewed = withContext(Dispatchers.IO) {
                database.viewHistoryDao().getMostViewed(since = sevenDaysAgo, limit = 5)
            }
            val restaurantMap = restaurants.associateBy { it.id }
            trendingRestaurants = mostViewed.mapNotNull { result ->
                restaurantMap[result.restaurantId]?.let { it to result.viewCount }
            }
        }
    }

    // Collect filter state
    val filterState by filterViewModel.filterState.collectAsState()

    // Handle restaurant click
    val onRestaurantClick: (Restaurant) -> Unit = { restaurant ->
        if (!filterState.hasActiveFilters) {
            val filterOptions = if (isInTorontoArea) {
                "Under \$15, Student Discount, or Near TTC"
            } else {
                "Under \$15 or Student Discount"
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Please select a filter ($filterOptions) first"
                )
            }
        } else {
            selectedRestaurant = restaurant
            // Record view for trending
            if (database != null) {
                scope.launch(Dispatchers.IO) {
                    database.viewHistoryDao().recordView(
                        ViewHistoryEntry(
                            restaurantId = restaurant.id,
                            source = ViewSource.SEARCH
                        )
                    )
                }
            }
        }
    }

    // Apply both search and chip filters - use derivedStateOf for efficient recomposition
    val displayedRestaurants by remember {
        derivedStateOf {
            FilterViewModel.applyFilters(searchFilteredRestaurants, filterState)
        }
    }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request permissions on launch
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Refresh function for pull-to-refresh
    val refreshRestaurants: suspend () -> Unit = {
        if (placesService != null && userLocation != null) {
            try {
                val nearbyRestaurants = withContext(Dispatchers.IO) {
                    placesService.searchNearbyRestaurants(userLocation!!)
                }
                if (nearbyRestaurants.isNotEmpty()) {
                    restaurants = nearbyRestaurants
                    searchFilteredRestaurants = restaurants
                }
            } catch (_: Exception) { }
        }
    }

    // Fetch location and restaurants when permissions granted
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            // Show sample data immediately, mark as not loading to show UI fast
            isLoadingRestaurants = false
            locationError = null

            try {
                // Get real GPS location
                val location = withContext(Dispatchers.IO) {
                    getCurrentLocation(context)
                }

                // Fallback to Toronto Union Station if location unavailable
                val finalLatitude = location?.latitude ?: 43.7515
                val finalLongitude = location?.longitude ?: -79.3440

                userLocation = LatLng(finalLatitude, finalLongitude)

                // Check if user is in Toronto area for TTC filter
                isInTorontoArea = TransitHelper.isInTorontoArea(finalLatitude, finalLongitude)

                // Fetch real restaurants if PlacesService available (in background)
                if (placesService != null) {
                    val nearbyRestaurants = withContext(Dispatchers.IO) {
                        placesService.searchNearbyRestaurants(
                            LatLng(finalLatitude, finalLongitude)
                        )
                    }
                    if (nearbyRestaurants.isNotEmpty()) {
                        restaurants = nearbyRestaurants
                        searchFilteredRestaurants = restaurants
                    }
                }
            } catch (e: Exception) {
                locationError = "Error: ${e.message}"
                // Keep showing sample data on error
            }
        } else {
            isLoadingRestaurants = false
        }
    }

    // Debounced smart search
    LaunchedEffect(searchQuery, restaurants) {
        if (searchQuery.isBlank()) {
            searchFilteredRestaurants = restaurants
            aiRecommendation = null
            return@LaunchedEffect
        }

        delay(800) // Increased debounce for better performance

        if (vertexAiService != null && restaurants.isNotEmpty()) {
            isSearching = true
            try {
                searchFilteredRestaurants = vertexAiService.getSearchSuggestions(searchQuery, restaurants)

                if (searchQuery.length > 15) {
                    aiRecommendation = vertexAiService.getRestaurantRecommendation(searchQuery, restaurants)
                }
            } finally {
                isSearching = false
            }
        } else {
            searchFilteredRestaurants = restaurants.filter { restaurant ->
                restaurant.name.contains(searchQuery, ignoreCase = true) ||
                        restaurant.cuisine.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Screen transition: detail slide-up vs home screen
    AnimatedContent(
        targetState = selectedRestaurant,
        transitionSpec = {
            if (targetState != null) {
                // Entering detail
                (slideInVertically(tween(300)) { it / 4 } + fadeIn(tween(300)))
                    .togetherWith(fadeOut(tween(200)))
            } else {
                // Exiting detail
                fadeIn(tween(300))
                    .togetherWith(slideOutVertically(tween(200)) { it / 4 } + fadeOut(tween(200)))
            }.using(SizeTransform(clip = false))
        },
        label = "screen_transition"
    ) { restaurant ->
        if (restaurant != null) {
            RestaurantDetailScreen(
                restaurant = restaurant,
                onBackClick = { selectedRestaurant = null }
            )
        } else {
            Scaffold(
                modifier = modifier,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "CheapEats",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            // Lunch Route button with text
                            androidx.compose.material3.TextButton(
                                onClick = { showLunchRoute = true },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = "Lunch",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // View mode toggle with text label
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                                },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = if (viewMode == ViewMode.LIST) "Map" else "List",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Menu"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        onClick = {
                                            showMenu = false
                                            showSettings = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Thank the Developer") },
                                        onClick = {
                                            showMenu = false
                                            showTipDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (viewMode == ViewMode.LIST) {
                        // First-time user tip
                        if (showFirstTimeTip) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tip: CheapEats works best near TTC stations during lunch hours",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { showFirstTimeTip = false }
                                    ) {
                                        Text("Got it")
                                    }
                                }
                            }
                        }

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (vertexAiService != null)
                                        "Try: \"cheap Mexican food\"..."
                                    else
                                        "Search restaurants..."
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Filter Bar - horizontal scrollable chips
                        // TTC filter only shown when user is in Toronto area
                        FilterBar(
                            filterViewModel = filterViewModel,
                            showTTCFilter = isInTorontoArea,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Pick card
                        aiRecommendation?.let { recommendation ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Quick Pick",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = recommendation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Active filters info with animated count (#5)
                        AnimatedVisibility(
                            visible = filterState.hasActiveFilters,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                AnimatedContent(
                                    targetState = displayedRestaurants.size,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            (slideInVertically { -it } + fadeIn())
                                                .togetherWith(slideOutVertically { it } + fadeOut())
                                        } else {
                                            (slideInVertically { it } + fadeIn())
                                                .togetherWith(slideOutVertically { -it } + fadeOut())
                                        }
                                    },
                                    label = "filter_count"
                                ) { count ->
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = " of ${searchFilteredRestaurants.size} restaurants",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // List ↔ Map crossfade (#2)
                    Crossfade(
                        targetState = viewMode,
                        animationSpec = tween(300),
                        label = "view_mode_crossfade"
                    ) { currentViewMode ->
                        when (currentViewMode) {
                            ViewMode.LIST -> {
                                // Content state transitions (#4)
                                val contentState = when {
                                    isLoadingRestaurants -> "loading"
                                    !locationPermissions.allPermissionsGranted -> "permission"
                                    else -> "content"
                                }

                                Crossfade(
                                    targetState = contentState,
                                    animationSpec = tween(400),
                                    label = "content_state_crossfade"
                                ) { state ->
                                    when (state) {
                                        "loading" -> {
                                            ShimmerRestaurantList(
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        "permission" -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(64.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "Location permission required",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Enable location to find restaurants near you",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            // Apply favorites to displayed restaurants
                                            val restaurantsWithFavorites = displayedRestaurants.map { r ->
                                                r.copy(isFavorite = r.id in favoriteIds)
                                            }

                                            PullToRefreshBox(
                                                isRefreshing = isRefreshing,
                                                onRefresh = {
                                                    scope.launch {
                                                        isRefreshing = true
                                                        refreshRestaurants()
                                                        isRefreshing = false
                                                    }
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                RestaurantList(
                                                    restaurants = restaurantsWithFavorites,
                                                    onRestaurantClick = onRestaurantClick,
                                                    onFavoriteToggle = { restaurantId ->
                                                        favoritesManager.toggleFavorite(restaurantId)
                                                    },
                                                    showPriceConfidence = filterState.isUnder15Active &&
                                                        filterState.priceFilterMode == com.parthipan.cheapeats.ui.filter.PriceFilterMode.FLEXIBLE,
                                                    trendingRestaurants = trendingRestaurants,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            ViewMode.MAP -> {
                                MapScreen(
                                    restaurants = displayedRestaurants,
                                    userLocation = userLocation,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Tip Dialog
    if (showTipDialog) {
        TipDialog(
            tipProducts = tipProducts,
            isLoading = purchaseState is PurchaseState.Processing,
            onTipSelected = { product ->
                (context as? Activity)?.let { activity ->
                    billingService.launchPurchaseFlow(activity, product)
                }
            },
            onDismiss = { showTipDialog = false }
        )
    }

    // Lunch Route Bottom Sheet
    if (showLunchRoute) {
        LunchRouteBottomSheet(
            state = lunchRouteState,
            isInTorontoArea = isInTorontoArea,
            onStartFromCurrentLocation = {
                userLocation?.let { loc ->
                    lunchRouteViewModel.selectStartLocation(RouteStart.CurrentLocation(loc))
                    lunchRouteViewModel.generatePlan(
                        restaurants = restaurants,
                        filterState = filterState,
                        userLocation = loc
                    )
                }
            },
            onStartFromStation = { station ->
                lunchRouteViewModel.selectStartLocation(RouteStart.TTCStation(station))
                lunchRouteViewModel.generatePlan(
                    restaurants = restaurants,
                    filterState = filterState,
                    userLocation = station.location
                )
            },
            onStartDirections = { restaurant ->
                // Use Google Maps navigation intent for turn-by-turn directions
                val lat = restaurant.location.latitude
                val lng = restaurant.location.longitude
                val uri = "google.navigation:q=$lat,$lng&mode=w"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to web URL if Google Maps not installed
                    val webUri = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUri)))
                }
            },
            onDismiss = {
                showLunchRoute = false
                lunchRouteViewModel.clearPlan()
            }
        )
    }

    // Settings Bottom Sheet
    if (showSettings && database != null) {
        SettingsBottomSheet(
            settings = userSettings ?: UserSettings(),
            cacheStats = cacheStats,
            onThemeModeChange = { mode ->
                scope.launch {
                    database.settingsDao().setThemeMode(mode)
                }
            },
            onSettingsChange = { newSettings ->
                scope.launch {
                    database.settingsDao().saveSettings(newSettings)
                }
            },
            onClearCache = {
                scope.launch {
                    // Clear cached restaurants
                    database.cacheDao().clearAllCache()
                    snackbarHostState.showSnackbar("Cache cleared")
                }
            },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun TipDialog(
    tipProducts: List<TipProduct>,
    isLoading: Boolean,
    onTipSelected: (TipProduct) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Thank the Developer",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "If you enjoy CheapEats, consider leaving a tip to support future development!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "This is completely optional - the app is free forever!",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (tipProducts.isEmpty()) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Tips not available yet.\nPlease try again later.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    tipProducts.forEach { product ->
                        OutlinedButton(
                            onClick = { onTipSelected(product) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(product.name)
                                Text(
                                    text = product.formattedPrice,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        }
    )
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): Location? {
    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).await()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun RestaurantList(
    restaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    showPriceConfidence: Boolean = false,
    trendingRestaurants: List<Pair<Restaurant, Int>> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Empty state
        AnimatedVisibility(
            visible = restaurants.isEmpty(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "No restaurants found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adjusting your filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Loaded state
        AnimatedVisibility(
            visible = restaurants.isNotEmpty(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trending section
                if (trendingRestaurants.size >= 3) {
                    item(key = "trending_section", contentType = "trending") {
                        TrendingSection(
                            trendingRestaurants = trendingRestaurants,
                            onRestaurantClick = onRestaurantClick
                        )
                    }
                }

                itemsIndexed(
                    items = restaurants,
                    key = { _, restaurant -> restaurant.id },
                    contentType = { _, _ -> "restaurant_card" }
                ) { index, restaurant ->
                    val visibleState = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }
                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                slideInVertically(tween(300, delayMillis = index * 50)) { it / 4 }
                    ) {
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant) },
                            onFavoriteToggle = { onFavoriteToggle(restaurant.id) },
                            showPriceConfidence = showPriceConfidence,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingSection(
    trendingRestaurants: List<Pair<Restaurant, Int>>,
    onRestaurantClick: (Restaurant) -> Unit
) {
    Column {
        Text(
            text = "Popular Near You",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = trendingRestaurants,
                key = { (restaurant, _) -> "trending_${restaurant.id}" }
            ) { (restaurant, viewCount) ->
                TrendingCard(
                    restaurant = restaurant,
                    viewCount = viewCount,
                    onClick = { onRestaurantClick(restaurant) }
                )
            }
        }
    }
}

@Composable
private fun TrendingCard(
    restaurant: Restaurant,
    viewCount: Int,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                val placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = restaurant.name.take(1),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (restaurant.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(restaurant.imageUrl)
                            .crossfade(200)
                            .size(300)
                            .memoryCacheKey("trending_${restaurant.id}")
                            .diskCacheKey("trending_${restaurant.id}")
                            .build(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = restaurant.cuisine,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // View count badge
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$viewCount views",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    showPriceConfidence: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Haptic feedback
    val hapticFeedback = LocalHapticFeedback.current

    // Card press animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 800f
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Full-width image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                // Letter placeholder background
                val placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = restaurant.name.take(1),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Load actual image on top
                if (restaurant.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(restaurant.imageUrl)
                            .crossfade(200)
                            .size(400) // Higher res for card image
                            .memoryCacheKey(restaurant.id)
                            .diskCacheKey(restaurant.id)
                            .build(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient overlay at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Restaurant name + cuisine on gradient
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Rating badge top-right
                if (restaurant.rating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", restaurant.rating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Favorite heart top-left with bounce animation
                var heartBounce by remember { mutableStateOf(false) }
                val heartBounceScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (heartBounce) 1.3f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.4f,
                        stiffness = 600f
                    ),
                    finishedListener = { heartBounce = false },
                    label = "heart_bounce"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            heartBounce = true
                            onFavoriteToggle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (restaurant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (restaurant.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (restaurant.isFavorite) Color(0xFFFF4444) else Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(heartBounceScale)
                    )
                }
            }

            // Content below image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Price + distance row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (restaurant.averagePrice != null) {
                        Text(
                            text = "~\$${String.format("%.0f", restaurant.averagePrice)} CAD",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (restaurant.priceLevel > 0) {
                        Text(
                            text = restaurant.pricePoint,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (showPriceConfidence && restaurant.priceConfidenceLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = restaurant.priceConfidenceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (restaurant.priceSource) {
                                com.parthipan.cheapeats.data.PriceSource.API_VERIFIED ->
                                    Color(0xFF4CAF50)
                                com.parthipan.cheapeats.data.PriceSource.ESTIMATED ->
                                    Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = String.format("%.1f km", restaurant.distance),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (restaurant.isSponsored) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tags row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Open Now indicator
                    if (restaurant.isOpenNow == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF4CAF50), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    if (restaurant.nearTTC) {
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "TTC",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFDA291C).copy(alpha = 0.1f),
                                labelColor = Color(0xFFDA291C)
                            ),
                            border = null
                        )
                    }

                    if (restaurant.hasStudentDiscount) {
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "Student",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.tertiary
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CheapEatsTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    CheapEatsTheme(darkTheme = true) {
        HomeScreen()
    }
}

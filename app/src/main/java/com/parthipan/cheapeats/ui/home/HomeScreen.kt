package com.parthipan.cheapeats.ui.home

import android.app.Activity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.parthipan.cheapeats.data.PlacesService
import com.parthipan.cheapeats.data.PurchaseState
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.TipProduct
import com.parthipan.cheapeats.data.TransitHelper
import com.parthipan.cheapeats.data.VertexAiService
import com.parthipan.cheapeats.data.sampleRestaurants
import com.parthipan.cheapeats.ui.detail.RestaurantDetailScreen
import com.parthipan.cheapeats.ui.filter.FilterBar
import com.parthipan.cheapeats.ui.filter.FilterViewModel
import com.parthipan.cheapeats.ui.map.MapScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme
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
    filterViewModel: FilterViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    // Navigation state for restaurant detail
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    // Menu and tip dialog state
    var showMenu by remember { mutableStateOf(false) }
    var showTipDialog by remember { mutableStateOf(false) }

    // Billing service
    val billingService = remember { BillingService(context) }
    val tipProducts by billingService.tipProducts.collectAsState()
    val purchaseState by billingService.purchaseState.collectAsState()

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

    // Fetch location and restaurants when permissions granted
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            // Show sample data immediately, mark as not loading to show UI fast
            isLoadingRestaurants = false
            locationError = null

            try {
                // TODO: TESTING - Using Toronto downtown location. Remove for production!
                // Downtown Toronto near Dundas Station
                val testLatitude = 43.6561
                val testLongitude = -79.3802
                val useTestLocation = false // Set to true for test location

                // Get location in background
                val location = withContext(Dispatchers.IO) {
                    if (useTestLocation) null else getCurrentLocation(context)
                }
                val finalLatitude = location?.latitude ?: testLatitude
                val finalLongitude = location?.longitude ?: testLongitude

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

    // Debounced AI search
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

    // Show detail screen if a restaurant is selected
    if (selectedRestaurant != null) {
        RestaurantDetailScreen(
            restaurant = selectedRestaurant!!,
            showUnder15Only = filterState.isUnder15Active,
            onBackClick = { selectedRestaurant = null }
        )
        return
    }

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
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.MAP else ViewMode.LIST
                        }
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.LIST) {
                                Icons.Default.LocationOn
                            } else {
                                Icons.AutoMirrored.Filled.List
                            },
                            contentDescription = if (viewMode == ViewMode.LIST) {
                                "Switch to Map View"
                            } else {
                                "Switch to List View"
                            }
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
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (vertexAiService != null)
                                "Ask AI: \"cheap Mexican food\"..."
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
                    shape = RoundedCornerShape(28.dp),
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

                // AI Recommendation card
                aiRecommendation?.let { recommendation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "AI Recommendation",
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

                // Active filters info
                if (filterState.hasActiveFilters) {
                    Text(
                        text = "${displayedRestaurants.size} of ${searchFilteredRestaurants.size} restaurants",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            when (viewMode) {
                ViewMode.LIST -> {
                    if (isLoadingRestaurants) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Finding restaurants near you...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else if (!locationPermissions.allPermissionsGranted) {
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
                    } else {
                        RestaurantList(
                            restaurants = displayedRestaurants,
                            onRestaurantClick = onRestaurantClick,
                            modifier = Modifier.fillMaxSize()
                        )
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
    modifier: Modifier = Modifier
) {
    if (restaurants.isEmpty()) {
        Box(
            modifier = modifier,
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
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(restaurants, key = { it.id }) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant) }
                )
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = restaurant.name.take(1),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (restaurant.isSponsored) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (restaurant.rating > 0) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", restaurant.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "  •  ",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = String.format("%.1f mi", restaurant.distance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (restaurant.averagePrice != null) {
                        Text(
                            text = "  •  ",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$${String.format("%.0f", restaurant.averagePrice)} avg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = restaurant.cuisine,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )

                    if (restaurant.priceLevel > 0) {
                        Text(
                            text = restaurant.pricePoint,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
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
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.error
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

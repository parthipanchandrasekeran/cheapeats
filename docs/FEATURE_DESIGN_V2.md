# CheapEats Feature Design v2.0
## Production-Ready Feature Improvements

---

## 1. AI Trust & Transparency

### 1.1 "Why This Pick?" Explanation Chips

**Goal:** Show users exactly why each restaurant was recommended with tappable explanation chips.

#### Data Model

```kotlin
// data/RecommendationReason.kt
enum class RecommendationReason(val label: String, val icon: String) {
    OPEN_NOW("Open now", "schedule"),
    VERIFIED_UNDER_15("Under $15 verified", "verified"),
    ESTIMATED_UNDER_15("~Under $15", "estimate"),
    NEAR_TTC("Near TTC", "train"),
    HIGH_RATING("Highly rated", "star"),
    QUERY_MATCH("Matches search", "search"),
    STUDENT_DISCOUNT("Student deal", "school"),
    QUICK_SERVICE("Fast service", "bolt"),
    LUNCH_SPECIAL("Lunch special", "restaurant")
}

// Extended Restaurant model
data class RankedRestaurant(
    val restaurant: Restaurant,
    val score: Float,
    val reasons: List<RecommendationReason>,  // Why we picked this
    val explanation: String                    // Human-readable summary
)
```

#### Reason Generation Logic

```kotlin
// data/ReasonGenerator.kt
object ReasonGenerator {

    fun generateReasons(
        restaurant: Restaurant,
        filterState: FilterState,
        searchQuery: String?
    ): List<RecommendationReason> {
        val reasons = mutableListOf<RecommendationReason>()

        // Open status
        if (restaurant.isOpenNow == true) {
            reasons.add(RecommendationReason.OPEN_NOW)
        }

        // Price verification
        when {
            restaurant.isVerifiedUnder15 ->
                reasons.add(RecommendationReason.VERIFIED_UNDER_15)
            restaurant.isFlexiblyUnder15 && restaurant.priceSource == PriceSource.ESTIMATED ->
                reasons.add(RecommendationReason.ESTIMATED_UNDER_15)
        }

        // TTC proximity
        if (restaurant.nearTTC && restaurant.ttcWalkMinutes?.let { it <= 5 } == true) {
            reasons.add(RecommendationReason.NEAR_TTC)
        }

        // Rating
        if (restaurant.rating >= 4.3f) {
            reasons.add(RecommendationReason.HIGH_RATING)
        }

        // Search match
        if (!searchQuery.isNullOrBlank() &&
            (restaurant.name.contains(searchQuery, ignoreCase = true) ||
             restaurant.cuisine.contains(searchQuery, ignoreCase = true))) {
            reasons.add(RecommendationReason.QUERY_MATCH)
        }

        // Student discount
        if (restaurant.hasStudentDiscount) {
            reasons.add(RecommendationReason.STUDENT_DISCOUNT)
        }

        return reasons.take(4)  // Max 4 chips to avoid clutter
    }

    fun generateExplanation(reasons: List<RecommendationReason>): String {
        if (reasons.isEmpty()) return "Nearby option"

        val parts = reasons.take(2).map { reason ->
            when (reason) {
                RecommendationReason.OPEN_NOW -> "open now"
                RecommendationReason.VERIFIED_UNDER_15 -> "verified cheap"
                RecommendationReason.NEAR_TTC -> "steps from transit"
                RecommendationReason.HIGH_RATING -> "locals love it"
                RecommendationReason.QUERY_MATCH -> "matches your search"
                else -> reason.label.lowercase()
            }
        }
        return parts.joinToString(", ").replaceFirstChar { it.uppercase() }
    }
}
```

#### UI Component

```kotlin
// ui/components/ReasonChips.kt
@Composable
fun ReasonChips(
    reasons: List<RecommendationReason>,
    modifier: Modifier = Modifier,
    onChipClick: ((RecommendationReason) -> Unit)? = null
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(reasons) { reason ->
            ReasonChip(
                reason = reason,
                onClick = { onChipClick?.invoke(reason) }
            )
        }
    }
}

@Composable
private fun ReasonChip(
    reason: RecommendationReason,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when (reason) {
            RecommendationReason.VERIFIED_UNDER_15 ->
                MaterialTheme.colorScheme.primaryContainer
            RecommendationReason.OPEN_NOW ->
                Color(0xFF4CAF50).copy(alpha = 0.15f)
            RecommendationReason.NEAR_TTC ->
                Color(0xFFDA291C).copy(alpha = 0.15f)  // TTC red
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = reason.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = reason.label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
```

### 1.2 Hard Filter Enforcement

**Goal:** AI recommendations must NEVER violate user's active filters.

#### Filter Contract

```kotlin
// data/FilterContract.kt
object FilterContract {

    /**
     * HARD FILTERS - These are NEVER violated by AI.
     * If a restaurant doesn't match, it's excluded entirely.
     */
    data class HardFilters(
        val mustBeOpen: Boolean = false,
        val strictUnder15: Boolean = false,  // Only verified prices
        val mustBeNearTTC: Boolean = false,
        val maxWalkMinutes: Int? = null
    )

    /**
     * SOFT PREFERENCES - These influence ranking but don't exclude.
     */
    data class SoftPreferences(
        val preferHighRating: Boolean = true,
        val preferVerifiedPrices: Boolean = true,
        val preferQuickService: Boolean = false
    )

    /**
     * Validates a restaurant against hard filters.
     * Returns null if restaurant should be excluded, or the restaurant if valid.
     */
    fun validateHardFilters(
        restaurant: Restaurant,
        filters: HardFilters
    ): Restaurant? {
        // Open Now - HARD FILTER
        if (filters.mustBeOpen && restaurant.isOpenNow != true) {
            return null  // Unknown or closed = excluded
        }

        // Strict Under $15 - HARD FILTER
        if (filters.strictUnder15 && !restaurant.isVerifiedUnder15) {
            return null  // Only verified prices pass
        }

        // Near TTC - HARD FILTER
        if (filters.mustBeNearTTC && !restaurant.nearTTC) {
            return null
        }

        // Max walk time - HARD FILTER
        if (filters.maxWalkMinutes != null) {
            val walkTime = restaurant.ttcWalkMinutes ?: Int.MAX_VALUE
            if (walkTime > filters.maxWalkMinutes) {
                return null
            }
        }

        return restaurant
    }
}
```

#### Integration with FilterViewModel

```kotlin
// In FilterViewModel.kt - Update applyFilters
companion object {
    fun applyFilters(
        restaurants: List<Restaurant>,
        state: FilterState
    ): List<Restaurant> {

        // Step 1: Build hard filters from state
        val hardFilters = FilterContract.HardFilters(
            mustBeOpen = state.isOpenNowActive,
            strictUnder15 = state.isUnder15Active &&
                           state.priceFilterMode == PriceFilterMode.STRICT,
            mustBeNearTTC = state.isNearTTCActive
        )

        // Step 2: Apply hard filters FIRST (no exceptions)
        val validated = restaurants.mapNotNull { restaurant ->
            FilterContract.validateHardFilters(restaurant, hardFilters)
        }

        // Step 3: Apply soft filters for flexible mode
        return if (state.isUnder15Active &&
                   state.priceFilterMode == PriceFilterMode.FLEXIBLE) {
            validated.filter { it.isFlexiblyUnder15 }
        } else {
            validated
        }
    }
}
```

---

## 2. Deals & Daily Specials

### 2.1 Data Model

```kotlin
// data/deals/Deal.kt
@Entity(tableName = "deals")
data class Deal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val restaurantId: String,
    val restaurantName: String,  // Denormalized for offline display

    val title: String,           // "2 Tacos for $5"
    val description: String?,    // "Every Tuesday 11am-3pm"
    val originalPrice: Float?,   // Strike-through price
    val dealPrice: Float,        // Must be < $15

    val dealType: DealType,
    val source: DealSource,

    // Time constraints
    val validDays: Int,          // Bitmask: Mon=1, Tue=2, Wed=4...
    val startTime: String?,      // "11:00" (24hr format)
    val endTime: String?,        // "15:00"
    val validFrom: Long?,        // Timestamp for limited-time deals
    val validUntil: Long?,       // Timestamp for expiry

    // Metadata
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val reportCount: Int = 0,
    val submittedBy: String?,    // User ID if user-submitted
    val verifiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DealType {
    DAILY_SPECIAL,      // Recurring daily deal
    WEEKLY_SPECIAL,     // Specific day(s) of week
    LIMITED_TIME,       // Has expiry date
    STUDENT_DISCOUNT,   // Requires student ID
    HAPPY_HOUR,         // Time-based
    COMBO_DEAL          // Bundle pricing
}

enum class DealSource {
    OFFICIAL,           // From restaurant API/website
    USER_SUBMITTED,     // Crowdsourced
    SCRAPED,           // Auto-discovered
    VERIFIED           // Staff verified
}
```

### 2.2 Room Database Structure

```kotlin
// data/database/DealDao.kt
@Dao
interface DealDao {

    @Query("""
        SELECT * FROM deals
        WHERE restaurantId = :restaurantId
        AND dealPrice < 15.0
        AND (validUntil IS NULL OR validUntil > :now)
        ORDER BY dealPrice ASC
    """)
    fun getDealsForRestaurant(restaurantId: String, now: Long): Flow<List<Deal>>

    @Query("""
        SELECT * FROM deals
        WHERE dealPrice < 15.0
        AND (validUntil IS NULL OR validUntil > :now)
        AND (
            validDays & :todayBitmask > 0
            OR validDays = 0
        )
        ORDER BY
            CASE WHEN validUntil IS NOT NULL THEN 0 ELSE 1 END,
            validUntil ASC,
            dealPrice ASC
        LIMIT :limit
    """)
    fun getActiveDealsToday(
        now: Long,
        todayBitmask: Int,
        limit: Int = 20
    ): Flow<List<Deal>>

    @Query("""
        SELECT * FROM deals
        WHERE validUntil IS NOT NULL
        AND validUntil > :now
        AND validUntil < :soonThreshold
        ORDER BY validUntil ASC
    """)
    fun getExpiringDeals(now: Long, soonThreshold: Long): Flow<List<Deal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: Deal)

    @Query("UPDATE deals SET upvotes = upvotes + 1 WHERE id = :dealId")
    suspend fun upvoteDeal(dealId: String)

    @Query("UPDATE deals SET downvotes = downvotes + 1 WHERE id = :dealId")
    suspend fun downvoteDeal(dealId: String)

    @Query("DELETE FROM deals WHERE validUntil < :now")
    suspend fun cleanupExpiredDeals(now: Long)
}
```

### 2.3 Time-Based Activation Logic

```kotlin
// data/deals/DealTimeHelper.kt
object DealTimeHelper {

    // Day bitmask constants
    const val MONDAY = 1
    const val TUESDAY = 2
    const val WEDNESDAY = 4
    const val THURSDAY = 8
    const val FRIDAY = 16
    const val SATURDAY = 32
    const val SUNDAY = 64
    const val WEEKDAYS = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY
    const val WEEKENDS = SATURDAY or SUNDAY
    const val ALL_DAYS = WEEKDAYS or WEEKENDS

    fun getTodayBitmask(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            Calendar.SATURDAY -> SATURDAY
            Calendar.SUNDAY -> SUNDAY
            else -> 0
        }
    }

    fun isDealActiveNow(deal: Deal): Boolean {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Check date validity
        if (deal.validFrom != null && now < deal.validFrom) return false
        if (deal.validUntil != null && now > deal.validUntil) return false

        // Check day of week
        val todayMask = getTodayBitmask()
        if (deal.validDays != 0 && (deal.validDays and todayMask) == 0) {
            return false
        }

        // Check time of day
        if (deal.startTime != null && deal.endTime != null) {
            val currentTime = String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
            if (currentTime < deal.startTime || currentTime > deal.endTime) {
                return false
            }
        }

        return true
    }

    fun getTimeRemainingText(deal: Deal): String? {
        if (deal.validUntil == null && deal.endTime == null) return null

        val now = System.currentTimeMillis()

        // Check if expiring today by end time
        if (deal.endTime != null && isDealActiveNow(deal)) {
            val endParts = deal.endTime.split(":")
            val endCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, endParts[0].toInt())
                set(Calendar.MINUTE, endParts[1].toInt())
                set(Calendar.SECOND, 0)
            }
            val minutesRemaining = (endCalendar.timeInMillis - now) / 60000

            return when {
                minutesRemaining <= 0 -> null
                minutesRemaining < 60 -> "Ends in ${minutesRemaining}min"
                minutesRemaining < 120 -> "Ends in 1hr ${minutesRemaining - 60}min"
                else -> "Until ${deal.endTime}"
            }
        }

        // Check absolute expiry
        if (deal.validUntil != null) {
            val hoursRemaining = (deal.validUntil - now) / 3600000
            return when {
                hoursRemaining <= 0 -> null
                hoursRemaining < 24 -> "Ends in ${hoursRemaining}hr"
                hoursRemaining < 48 -> "Ends tomorrow"
                else -> null
            }
        }

        return null
    }
}
```

### 2.4 User-Submitted Deals

```kotlin
// data/deals/DealSubmission.kt
data class DealSubmission(
    val restaurantId: String,
    val title: String,
    val dealPrice: Float,
    val originalPrice: Float?,
    val description: String?,
    val dealType: DealType,
    val validDays: Int = DealTimeHelper.ALL_DAYS,
    val startTime: String? = null,
    val endTime: String? = null,
    val validUntil: Long? = null,
    val photoUri: Uri? = null
)

// In DealRepository.kt
class DealRepository(
    private val dealDao: DealDao,
    private val userPrefs: UserPreferences
) {

    suspend fun submitDeal(submission: DealSubmission): Result<Deal> {
        // Validation
        if (submission.dealPrice >= 15f) {
            return Result.failure(IllegalArgumentException("Deal must be under $15"))
        }

        if (submission.title.length < 5) {
            return Result.failure(IllegalArgumentException("Title too short"))
        }

        val deal = Deal(
            restaurantId = submission.restaurantId,
            restaurantName = "", // Will be filled from restaurant lookup
            title = submission.title,
            description = submission.description,
            originalPrice = submission.originalPrice,
            dealPrice = submission.dealPrice,
            dealType = submission.dealType,
            source = DealSource.USER_SUBMITTED,
            validDays = submission.validDays,
            startTime = submission.startTime,
            endTime = submission.endTime,
            validUntil = submission.validUntil,
            submittedBy = userPrefs.getUserId()
        )

        dealDao.insertDeal(deal)
        return Result.success(deal)
    }

    suspend fun voteDeal(dealId: String, isUpvote: Boolean) {
        if (isUpvote) {
            dealDao.upvoteDeal(dealId)
        } else {
            dealDao.downvoteDeal(dealId)
        }
    }
}
```

### 2.5 Deal Card UI

```kotlin
// ui/deals/DealCard.kt
@Composable
fun DealCard(
    deal: Deal,
    onVote: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeRemaining = remember(deal) { DealTimeHelper.getTimeRemainingText(deal) }
    val isActive = remember(deal) { DealTimeHelper.isDealActiveNow(deal) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deal.restaurantName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price display
                Column(horizontalAlignment = Alignment.End) {
                    if (deal.originalPrice != null) {
                        Text(
                            text = "$${String.format("%.2f", deal.originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", deal.dealPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Time indicator
            if (timeRemaining != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // Source badge + voting
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source indicator
                Text(
                    text = when (deal.source) {
                        DealSource.OFFICIAL -> "Official"
                        DealSource.VERIFIED -> "Verified"
                        DealSource.USER_SUBMITTED -> "User tip"
                        DealSource.SCRAPED -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Voting for user-submitted deals
                if (deal.source == DealSource.USER_SUBMITTED) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onVote(true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Upvote",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${deal.upvotes - deal.downvotes}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        IconButton(
                            onClick = { onVote(false) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Downvote",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

## 3. Budget Map Enhancements

### 3.1 "Closest Under $15" Quick Action

```kotlin
// ui/map/MapQuickActions.kt
@Composable
fun MapQuickActions(
    userLocation: LatLng?,
    restaurants: List<Restaurant>,
    onRestaurantSelected: (Restaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    val closestCheap = remember(userLocation, restaurants) {
        if (userLocation == null) return@remember null

        restaurants
            .filter { it.isVerifiedUnder15 || it.isFlexiblyUnder15 }
            .filter { it.isOpenNow == true }
            .minByOrNull { restaurant ->
                SphericalUtil.computeDistanceBetween(
                    userLocation,
                    restaurant.location
                )
            }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Closest Under $15 button
        FilledTonalButton(
            onClick = { closestCheap?.let { onRestaurantSelected(it) } },
            enabled = closestCheap != null,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Closest < $15")
        }

        // Near TTC button
        OutlinedButton(
            onClick = { /* Navigate to TTC mode */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ttc),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Near TTC")
        }
    }
}
```

### 3.2 Cheap-Area Density Hints (Lightweight)

**Goal:** Show areas with many cheap options without expensive heatmap rendering.

```kotlin
// data/map/CheapAreaHint.kt
data class CheapAreaHint(
    val center: LatLng,
    val radius: Float,           // meters
    val restaurantCount: Int,
    val avgPrice: Float,
    val label: String            // "5 spots under $12"
)

// data/map/CheapAreaCalculator.kt
object CheapAreaCalculator {

    private const val CLUSTER_RADIUS_METERS = 300.0
    private const val MIN_CLUSTER_SIZE = 3

    fun calculateCheapAreas(
        restaurants: List<Restaurant>,
        bounds: LatLngBounds
    ): List<CheapAreaHint> {
        // Filter to cheap restaurants in view
        val cheapRestaurants = restaurants.filter {
            it.isFlexiblyUnder15 && bounds.contains(it.location)
        }

        if (cheapRestaurants.size < MIN_CLUSTER_SIZE) return emptyList()

        // Simple grid-based clustering (not k-means, too heavy)
        val gridSize = 0.003  // ~300m at Toronto latitude
        val clusters = cheapRestaurants
            .groupBy { r ->
                val latBucket = (r.location.latitude / gridSize).toInt()
                val lngBucket = (r.location.longitude / gridSize).toInt()
                latBucket to lngBucket
            }
            .filter { it.value.size >= MIN_CLUSTER_SIZE }

        return clusters.map { (_, clusterRestaurants) ->
            val avgLat = clusterRestaurants.map { it.location.latitude }.average()
            val avgLng = clusterRestaurants.map { it.location.longitude }.average()
            val avgPrice = clusterRestaurants.mapNotNull { it.averagePrice }.average()

            CheapAreaHint(
                center = LatLng(avgLat, avgLng),
                radius = CLUSTER_RADIUS_METERS.toFloat(),
                restaurantCount = clusterRestaurants.size,
                avgPrice = avgPrice.toFloat(),
                label = "${clusterRestaurants.size} spots ~$${avgPrice.toInt()}"
            )
        }
    }
}
```

#### Map Overlay Component

```kotlin
// ui/map/CheapAreaOverlay.kt
@Composable
fun CheapAreaOverlay(
    hints: List<CheapAreaHint>,
    cameraPosition: CameraPositionState
) {
    // Only show when zoomed out enough
    val showHints = cameraPosition.position.zoom < 15f

    if (showHints) {
        hints.forEach { hint ->
            // Simple circle with count badge
            Circle(
                center = hint.center,
                radius = hint.radius.toDouble(),
                fillColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                strokeColor = Color(0xFF4CAF50).copy(alpha = 0.4f),
                strokeWidth = 2f
            )

            // Marker with count
            Marker(
                state = MarkerState(position = hint.center),
                icon = BitmapDescriptorFactory.fromBitmap(
                    createCountBadge(hint.restaurantCount)
                ),
                anchor = Offset(0.5f, 0.5f),
                onClick = { /* Zoom in to area */ true }
            )
        }
    }
}

private fun createCountBadge(count: Int): Bitmap {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Green circle
    val paint = Paint().apply {
        color = android.graphics.Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint)

    // White text
    paint.apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(
        count.toString(),
        size / 2f,
        size / 2f + 7,
        paint
    )

    return bitmap
}
```

### 3.3 Price Confidence on Map Info Cards

```kotlin
// ui/map/MapInfoCard.kt
@Composable
fun MapInfoCard(
    restaurant: Restaurant,
    onNavigateClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price with confidence
                PriceConfidenceBadge(restaurant = restaurant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick info row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Open status
                OpenStatusBadge(isOpen = restaurant.isOpenNow)

                // Distance
                restaurant.distance?.let { dist ->
                    Text(
                        text = "${String.format("%.1f", dist)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // TTC walk time
                restaurant.ttcWalkMinutes?.let { mins ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ttc),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFDA291C)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${mins}min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate")
                }

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Menu")
                }
            }
        }
    }
}

@Composable
private fun PriceConfidenceBadge(restaurant: Restaurant) {
    Column(horizontalAlignment = Alignment.End) {
        // Price
        Text(
            text = restaurant.averagePrice?.let {
                "~$${String.format("%.0f", it)}"
            } ?: restaurant.pricePoint,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (restaurant.isVerifiedUnder15) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        // Confidence label
        if (restaurant.averagePrice != null) {
            Text(
                text = restaurant.priceConfidenceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = when (restaurant.priceSource) {
                    PriceSource.API_VERIFIED -> Color(0xFF4CAF50)
                    PriceSource.ESTIMATED -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun OpenStatusBadge(isOpen: Boolean?) {
    val (text, color) = when (isOpen) {
        true -> "Open" to Color(0xFF4CAF50)
        false -> "Closed" to Color(0xFFF44336)
        null -> "Hours unknown" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
```

---

## 4. Smarter Favorites - Collections System

### 4.1 Data Model

```kotlin
// data/favorites/Collection.kt
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,            // Material icon name
    val colorHex: String,        // "#4CAF50"
    val isSystem: Boolean,       // true for default collections
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)

// Predefined system collections
object SystemCollections {
    val FAVORITES = Collection(
        id = "favorites",
        name = "Favorites",
        icon = "favorite",
        colorHex = "#F44336",
        isSystem = true,
        sortOrder = 0
    )
    val LUNCH = Collection(
        id = "lunch",
        name = "Lunch Spots",
        icon = "lunch_dining",
        colorHex = "#FF9800",
        isSystem = true,
        sortOrder = 1
    )
    val LATE_NIGHT = Collection(
        id = "late_night",
        name = "Late Night",
        icon = "nightlife",
        colorHex = "#9C27B0",
        isSystem = true,
        sortOrder = 2
    )
    val VEGETARIAN = Collection(
        id = "vegetarian",
        name = "Vegetarian",
        icon = "eco",
        colorHex = "#4CAF50",
        isSystem = true,
        sortOrder = 3
    )
    val QUICK_BITES = Collection(
        id = "quick_bites",
        name = "Quick Bites",
        icon = "bolt",
        colorHex = "#2196F3",
        isSystem = true,
        sortOrder = 4
    )

    val ALL = listOf(FAVORITES, LUNCH, LATE_NIGHT, VEGETARIAN, QUICK_BITES)
}

// Junction table for many-to-many relationship
@Entity(
    tableName = "collection_restaurants",
    primaryKeys = ["collectionId", "restaurantId"]
)
data class CollectionRestaurant(
    val collectionId: String,
    val restaurantId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val note: String? = null     // User note for this restaurant in this collection
)
```

### 4.2 Room DAOs

```kotlin
// data/database/CollectionDao.kt
@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY sortOrder")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollection(id: String): Collection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    // Get all restaurants in a collection
    @Query("""
        SELECT r.* FROM restaurants r
        INNER JOIN collection_restaurants cr ON r.id = cr.restaurantId
        WHERE cr.collectionId = :collectionId
        ORDER BY cr.addedAt DESC
    """)
    fun getRestaurantsInCollection(collectionId: String): Flow<List<Restaurant>>

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
        SELECT c.*, COUNT(cr.restaurantId) as restaurantCount
        FROM collections c
        LEFT JOIN collection_restaurants cr ON c.id = cr.collectionId
        GROUP BY c.id
        ORDER BY c.sortOrder
    """)
    fun getCollectionsWithCounts(): Flow<List<CollectionWithCount>>
}

data class CollectionWithCount(
    @Embedded val collection: Collection,
    val restaurantCount: Int
)
```

### 4.3 Repeat Protection

```kotlin
// data/favorites/ViewHistory.kt
@Entity(tableName = "view_history")
data class ViewHistoryEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val restaurantId: String,
    val viewedAt: Long = System.currentTimeMillis(),
    val source: ViewSource          // How user encountered it
)

enum class ViewSource {
    SEARCH,         // User searched for it
    RECOMMENDATION, // Shown in recommendations
    MAP_TAP,        // Tapped on map
    COLLECTION      // Opened from collection
}

@Dao
interface ViewHistoryDao {

    @Insert
    suspend fun recordView(entry: ViewHistoryEntry)

    @Query("""
        SELECT restaurantId FROM view_history
        WHERE viewedAt > :since
        AND source = 'RECOMMENDATION'
    """)
    suspend fun getRecentlyRecommended(since: Long): List<String>

    @Query("DELETE FROM view_history WHERE viewedAt < :before")
    suspend fun cleanupOldHistory(before: Long)
}

// data/favorites/RepeatProtection.kt
class RepeatProtection(
    private val viewHistoryDao: ViewHistoryDao
) {

    private companion object {
        const val COOLDOWN_HOURS = 24
        const val MAX_REPEATS_PER_DAY = 2
    }

    /**
     * Filter out restaurants that were recently shown in recommendations.
     * Restaurants accessed via search are NOT filtered.
     */
    suspend fun filterRecentlyShown(
        restaurants: List<Restaurant>,
        preserveSearched: Boolean = true
    ): List<Restaurant> {
        val since = System.currentTimeMillis() - (COOLDOWN_HOURS * 3600000L)
        val recentIds = viewHistoryDao.getRecentlyRecommended(since).toSet()

        return restaurants.filter { restaurant ->
            restaurant.id !in recentIds
        }
    }

    /**
     * Record that a restaurant was shown/viewed.
     */
    suspend fun recordView(restaurantId: String, source: ViewSource) {
        viewHistoryDao.recordView(
            ViewHistoryEntry(
                restaurantId = restaurantId,
                source = source
            )
        )
    }

    /**
     * Cleanup old history entries (call periodically).
     */
    suspend fun cleanup() {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 3600000L)
        viewHistoryDao.cleanupOldHistory(weekAgo)
    }
}
```

### 4.4 Collection Picker UI

```kotlin
// ui/favorites/CollectionPicker.kt
@Composable
fun CollectionPickerDialog(
    restaurantId: String,
    restaurantName: String,
    currentCollections: List<Collection>,
    allCollections: List<CollectionWithCount>,
    onToggleCollection: (Collection, Boolean) -> Unit,
    onCreateCollection: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Collection") },
        text = {
            Column {
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(allCollections) { collectionWithCount ->
                        val isSelected = currentCollections.any {
                            it.id == collectionWithCount.collection.id
                        }

                        CollectionToggleRow(
                            collection = collectionWithCount.collection,
                            count = collectionWithCount.restaurantCount,
                            isSelected = isSelected,
                            onToggle = {
                                onToggleCollection(collectionWithCount.collection, !isSelected)
                            }
                        )
                    }

                    item {
                        TextButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Collection")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    if (showCreateDialog) {
        CreateCollectionDialog(
            onCreate = { name ->
                onCreateCollection(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun CollectionToggleRow(
    collection: Collection,
    count: Int,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collection icon with color
        Surface(
            shape = CircleShape,
            color = Color(android.graphics.Color.parseColor(collection.colorHex))
                .copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = collection.icon.toImageVector(),
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(collection.colorHex)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$count restaurants",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}
```

---

## 5. Offline / Low-Data Mode

### 5.1 Cached Results Table

```kotlin
// data/cache/CachedRestaurant.kt
@Entity(tableName = "cached_restaurants")
data class CachedRestaurant(
    @PrimaryKey
    val id: String,

    // Core data (always cached)
    val name: String,
    val cuisine: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val priceLevel: Int,
    val rating: Float,
    val nearTTC: Boolean,
    val hasStudentDiscount: Boolean,

    // Price data
    val averagePrice: Float?,
    val priceSource: String,    // PriceSource enum name

    // Open hours (cached but may be stale)
    val isOpenNow: Boolean?,
    val openingHoursJson: String?,  // JSON of weekly hours

    // Images
    val imageUrl: String?,
    val thumbnailPath: String?,     // Local file path if cached

    // Cache metadata
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val dataFreshness: String = DataFreshness.CACHED.name,

    // Location context (for relevance sorting)
    val cachedNearLat: Double?,     // User's location when cached
    val cachedNearLng: Double?
)

// Conversion extensions
fun Restaurant.toCached(userLocation: LatLng?): CachedRestaurant {
    return CachedRestaurant(
        id = id,
        name = name,
        cuisine = cuisine,
        address = address,
        latitude = location.latitude,
        longitude = location.longitude,
        priceLevel = priceLevel,
        rating = rating,
        nearTTC = nearTTC,
        hasStudentDiscount = hasStudentDiscount,
        averagePrice = averagePrice,
        priceSource = priceSource.name,
        isOpenNow = isOpenNow,
        openingHoursJson = null,  // TODO: serialize if available
        imageUrl = imageUrl,
        thumbnailPath = null,
        cachedNearLat = userLocation?.latitude,
        cachedNearLng = userLocation?.longitude
    )
}

fun CachedRestaurant.toRestaurant(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        cuisine = cuisine,
        priceLevel = priceLevel,
        rating = rating,
        distance = null,  // Recalculate from current location
        imageUrl = thumbnailPath ?: imageUrl,
        address = address,
        location = LatLng(latitude, longitude),
        isSponsored = false,
        hasStudentDiscount = hasStudentDiscount,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        priceSource = PriceSource.valueOf(priceSource),
        isOpenNow = isOpenNow,
        dataFreshness = DataFreshness.CACHED
    )
}
```

### 5.2 Cache DAO

```kotlin
// data/database/CacheDao.kt
@Dao
interface CacheDao {

    @Query("""
        SELECT * FROM cached_restaurants
        ORDER BY lastAccessedAt DESC
        LIMIT :limit
    """)
    fun getRecentlyViewed(limit: Int = 50): Flow<List<CachedRestaurant>>

    @Query("""
        SELECT * FROM cached_restaurants
        WHERE (
            (:lat - latitude) * (:lat - latitude) +
            (:lng - longitude) * (:lng - longitude)
        ) < :radiusSquared
        ORDER BY rating DESC
        LIMIT :limit
    """)
    fun getNearby(
        lat: Double,
        lng: Double,
        radiusSquared: Double = 0.001,  // ~100m at Toronto
        limit: Int = 30
    ): Flow<List<CachedRestaurant>>

    @Query("""
        SELECT * FROM cached_restaurants
        WHERE averagePrice IS NOT NULL AND averagePrice < 15.0
        ORDER BY averagePrice ASC
        LIMIT :limit
    """)
    fun getCheapestCached(limit: Int = 20): Flow<List<CachedRestaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRestaurant(restaurant: CachedRestaurant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRestaurants(restaurants: List<CachedRestaurant>)

    @Query("UPDATE cached_restaurants SET lastAccessedAt = :now WHERE id = :id")
    suspend fun touchRestaurant(id: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM cached_restaurants WHERE cachedAt < :before")
    suspend fun cleanupOldCache(before: Long)

    @Query("SELECT SUM(LENGTH(thumbnailPath)) FROM cached_restaurants WHERE thumbnailPath IS NOT NULL")
    suspend fun getCachedImageSize(): Long?

    @Query("SELECT COUNT(*) FROM cached_restaurants")
    suspend fun getCacheCount(): Int
}
```

### 5.3 Offline Manager

```kotlin
// data/cache/OfflineManager.kt
class OfflineManager(
    private val context: Context,
    private val cacheDao: CacheDao,
    private val connectivityManager: ConnectivityManager
) {

    private companion object {
        const val MAX_CACHE_AGE_DAYS = 7
        const val MAX_CACHE_SIZE_MB = 50
        const val MAX_CACHED_RESTAURANTS = 200
        const val THUMBNAIL_SIZE = 200
    }

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()

    init {
        // Monitor connectivity
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOffline.value = false
            }
            override fun onLost(network: Network) {
                _isOffline.value = true
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
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

        // Cache thumbnails in background
        restaurants.forEach { restaurant ->
            restaurant.imageUrl?.let { url ->
                cacheThumbnail(restaurant.id, url)
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
                // Apply filters to cached data
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
                    val bitmap = (result.drawable as BitmapDrawable).bitmap
                    val file = File(context.cacheDir, "thumbnails/$restaurantId.jpg")
                    file.parentFile?.mkdirs()

                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }

                    // Update cache entry with local path
                    cacheDao.cacheRestaurant(
                        cacheDao.getNearby(0.0, 0.0, Double.MAX_VALUE, 1)
                            .first()
                            .firstOrNull { it.id == restaurantId }
                            ?.copy(thumbnailPath = file.absolutePath)
                            ?: return@withContext
                    )
                }
            } catch (e: Exception) {
                // Silently fail - thumbnail caching is best-effort
            }
        }
    }

    /**
     * Cleanup old cached data.
     */
    suspend fun cleanup() {
        val weekAgo = System.currentTimeMillis() - (MAX_CACHE_AGE_DAYS * 24 * 3600000L)
        cacheDao.cleanupOldCache(weekAgo)

        // Clean up orphaned thumbnail files
        val cachedIds = cacheDao.getRecentlyViewed(Int.MAX_VALUE).first().map { it.id }.toSet()
        File(context.cacheDir, "thumbnails").listFiles()?.forEach { file ->
            val id = file.nameWithoutExtension
            if (id !in cachedIds) {
                file.delete()
            }
        }

        updateStats()
    }

    private suspend fun updateStats() {
        _cacheStats.value = CacheStats(
            restaurantCount = cacheDao.getCacheCount(),
            imageSizeBytes = cacheDao.getCachedImageSize() ?: 0
        )
    }
}

data class CacheStats(
    val restaurantCount: Int = 0,
    val imageSizeBytes: Long = 0
) {
    val formattedSize: String
        get() = when {
            imageSizeBytes < 1024 -> "${imageSizeBytes}B"
            imageSizeBytes < 1024 * 1024 -> "${imageSizeBytes / 1024}KB"
            else -> "${imageSizeBytes / (1024 * 1024)}MB"
        }
}
```

### 5.4 Offline UI Indicator

```kotlin
// ui/components/OfflineBanner.kt
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    cacheStats: CacheStats,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            color = Color(0xFFFF9800),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Offline mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "· ${cacheStats.restaurantCount} cached",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
```

### 5.5 Low-Data Mode Settings

```kotlin
// data/settings/DataSettings.kt
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 0,  // Single row
    val lowDataMode: Boolean = false,
    val cacheImagesOnWifi: Boolean = true,
    val maxCacheSizeMb: Int = 50,
    val prefetchNearby: Boolean = true
)

// In SettingsScreen.kt
@Composable
fun DataSettingsSection(
    settings: UserSettings,
    cacheStats: CacheStats,
    onSettingsChange: (UserSettings) -> Unit,
    onClearCache: () -> Unit
) {
    Column {
        Text(
            text = "Data & Storage",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Low Data Mode toggle
        SwitchPreference(
            title = "Low Data Mode",
            subtitle = "Reduce image quality and prefetching",
            checked = settings.lowDataMode,
            onCheckedChange = {
                onSettingsChange(settings.copy(lowDataMode = it))
            }
        )

        // Cache images on WiFi only
        SwitchPreference(
            title = "Cache images on WiFi only",
            subtitle = "Save mobile data",
            checked = settings.cacheImagesOnWifi,
            onCheckedChange = {
                onSettingsChange(settings.copy(cacheImagesOnWifi = it))
            }
        )

        // Prefetch nearby
        SwitchPreference(
            title = "Prefetch nearby restaurants",
            subtitle = "Better offline experience",
            checked = settings.prefetchNearby,
            onCheckedChange = {
                onSettingsChange(settings.copy(prefetchNearby = it))
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Cache info
        ListItem(
            headlineContent = { Text("Cached data") },
            supportingContent = {
                Text("${cacheStats.restaurantCount} restaurants · ${cacheStats.formattedSize}")
            },
            trailingContent = {
                TextButton(onClick = onClearCache) {
                    Text("Clear")
                }
            }
        )
    }
}
```

---

## Implementation Priority

| Phase | Features | Effort |
|-------|----------|--------|
| **1** | Hard Filter Enforcement, "Why this pick?" chips | Medium |
| **2** | Offline caching, Low-data mode | Medium |
| **3** | Collections upgrade from Favorites | Medium |
| **4** | Map enhancements (quick action, density hints) | Low |
| **5** | Deals & Daily Specials system | High |

---

## Database Migration

```kotlin
// data/database/AppDatabase.kt
@Database(
    entities = [
        CachedRestaurant::class,
        Collection::class,
        CollectionRestaurant::class,
        ViewHistoryEntry::class,
        Deal::class,
        UserSettings::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    abstract fun collectionDao(): CollectionDao
    abstract fun viewHistoryDao(): ViewHistoryDao
    abstract fun dealDao(): DealDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS collections (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        isSystem INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS collection_restaurants (
                        collectionId TEXT NOT NULL,
                        restaurantId TEXT NOT NULL,
                        addedAt INTEGER NOT NULL,
                        note TEXT,
                        PRIMARY KEY (collectionId, restaurantId)
                    )
                """)

                // Migrate old favorites to new collection
                database.execSQL("""
                    INSERT INTO collections (id, name, icon, colorHex, isSystem, sortOrder, createdAt)
                    VALUES ('favorites', 'Favorites', 'favorite', '#F44336', 1, 0, ${System.currentTimeMillis()})
                """)

                // ... other table creations
            }
        }
    }
}
```

---

## Summary

This design provides:

1. **AI Trust** - Transparent "why this pick?" explanations with hard filter guarantees
2. **Deals** - Full-featured daily specials with time-based activation and crowdsourcing
3. **Map** - Quick "closest cheap" action and lightweight density hints
4. **Favorites** - Multi-collection system with repeat protection
5. **Offline** - Robust caching with configurable low-data mode

All features follow Android best practices with Room for persistence, Kotlin coroutines for async operations, and Jetpack Compose for UI.

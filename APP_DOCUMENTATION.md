# CheapEats - App Documentation

## Overview

CheapEats is a native Android application designed to help Toronto users find affordable dining options under $15 CAD. The app integrates real-time restaurant data, AI-powered recommendations, and TTC transit information to provide practical, budget-friendly dining suggestions.

**Package:** `com.parthipan.cheapeats`
**Min SDK:** 24 (Android 7.0)
**Target SDK:** 36 (Android 15)
**Language:** Kotlin 2.0.21
**UI Framework:** Jetpack Compose with Material Design 3

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
app/src/main/java/com/parthipan/cheapeats/
├── MainActivity.kt                    # Entry point
├── data/                              # Business logic & data models
│   ├── Restaurant.kt                  # Restaurant data model
│   ├── RestaurantRanker.kt            # Ranking algorithm
│   ├── ReasonGenerator.kt             # "Why this pick?" reasons
│   ├── FilterContract.kt              # Hard filter enforcement
│   ├── RecommendationReason.kt        # Recommendation reason enum
│   ├── FavoritesManager.kt            # Favorites persistence
│   ├── PlacesService.kt               # Google Places API
│   ├── VertexAiService.kt             # AI recommendations
│   ├── TransitHelper.kt               # TTC station data
│   ├── BillingService.kt              # In-app purchases
│   ├── cache/                         # Offline caching
│   │   ├── CachedRestaurant.kt        # Cached entity
│   │   └── OfflineManager.kt          # Connectivity & cache management
│   ├── deals/                         # Deals & specials
│   │   ├── Deal.kt                    # Deal entity
│   │   ├── DealTimeHelper.kt          # Day bitmask & time logic
│   │   └── DealRepository.kt          # Deal business logic
│   ├── favorites/                     # Collections system
│   │   ├── Collection.kt              # Collection entity
│   │   ├── CollectionRestaurant.kt    # Junction table
│   │   ├── ViewHistory.kt             # View tracking
│   │   ├── RepeatProtection.kt        # 24hr cooldown
│   │   └── CollectionRepository.kt    # Business logic
│   ├── map/                           # Map enhancements
│   │   └── CheapAreaHint.kt           # Cheap area calculator
│   └── settings/                      # User settings
│       └── UserSettings.kt            # Settings entity
├── database/                          # Room persistence layer
│   ├── AppDatabase.kt                 # Main database
│   ├── CacheDao.kt                    # Cache operations
│   ├── DealDao.kt                     # Deal operations
│   ├── CollectionDao.kt               # Collection operations
│   ├── ViewHistoryDao.kt              # View history operations
│   └── SettingsDao.kt                 # Settings operations
└── ui/                                # Jetpack Compose UI
    ├── home/                          # Main screen with list
    ├── filter/                        # Filter components
    ├── detail/                        # Restaurant detail
    ├── map/                           # Map view & overlays
    ├── deals/                         # Deal cards
    ├── favorites/                     # Collection picker
    ├── components/                    # Shared components
    ├── settings/                      # Settings UI
    └── theme/                         # Material 3 theming
```

---

## Core Features

### 1. Restaurant Discovery
- Real-time search via Google Places API (New)
- 1.5km radius search from user location
- Returns up to 20 nearby restaurants
- Displays rating, price level, cuisine type, distance
- Restaurant photos loaded via Coil image library

### 2. AI-Powered Search
- Vertex AI (Gemini 1.5 Flash) integration
- Semantic search understanding ("cheap Mexican food")
- Context-aware recommendations for Toronto
- Time-aware suggestions (lunch vs evening)
- Budget-conscious filtering

### 3. Smart Filtering
| Filter | Description |
|--------|-------------|
| Open Now | Shows currently open restaurants |
| Under $15 | Filters to budget-friendly options |
| Student Discount | Shows places with student deals |
| Near TTC | Within 500m of subway station |

**Price Filter Modes:**
- **Strict Mode** - Only shows API-verified prices under $15
- **Flexible Mode** - Includes estimated prices with confidence indicators

### 4. AI Trust & Transparency

#### "Why This Pick?" Chips
Each recommendation displays up to 4 reason chips explaining why it was suggested:
- **Open now** - Restaurant currently open
- **Under $15 verified** - Price confirmed by API
- **~Under $15** - Price estimated
- **Near TTC** - Within 5 min walk of station
- **Highly rated** - 4.3+ star rating
- **Matches search** - Matches user's query
- **Student deal** - Student discount available

#### Filter Contract
Hard filter enforcement ensures AI never violates active user filters:
```kotlin
object FilterContract {
    data class HardFilters(
        val mustBeOpen: Boolean = false,
        val strictUnder15: Boolean = false,
        val mustBeNearTTC: Boolean = false,
        val maxWalkMinutes: Int? = null
    )
}
```

### 5. Deals & Daily Specials

#### Deal Types
- Daily specials
- Weekly specials
- Limited time offers
- Student discounts
- Happy hour deals
- Combo deals

#### Day Bitmask System
Deals use a bitmask for valid days:
```kotlin
const val MONDAY = 1      // 0000001
const val TUESDAY = 2     // 0000010
const val WEDNESDAY = 4   // 0000100
const val THURSDAY = 8    // 0001000
const val FRIDAY = 16     // 0010000
const val SATURDAY = 32   // 0100000
const val SUNDAY = 64     // 1000000
const val WEEKDAYS = 31   // Mon-Fri
const val WEEKENDS = 96   // Sat-Sun
const val ALL_DAYS = 127  // Every day
```

#### Deal Sources
- **Official** - From restaurant directly
- **Verified** - Confirmed by multiple users
- **User Submitted** - Community tips with voting
- **Scraped** - Automated discovery

### 6. Smarter Favorites (Collections)

#### System Collections
- **Favorites** - Default heart icon collection
- **Lunch Spots** - Quick lunch options
- **Late Night** - After-hours dining
- **Vegetarian** - Plant-based options
- **Quick Bites** - Fast service restaurants

#### Features
- Custom collection creation
- Many-to-many relationship (restaurants in multiple collections)
- View history tracking
- 24-hour repeat protection (prevents showing same restaurants repeatedly)

### 7. Dual View Modes
- **List View**: Scrollable restaurant cards with search and filters
- **Map View**: Google Map with clustered markers (gold = sponsored, red = standard)

### 8. Budget Map Enhancements

#### "Closest Under $15" Quick Action
One-tap to find nearest affordable restaurant from current location.

#### Price Confidence on Map
Map info cards show price source confidence:
- Green: API verified
- Orange: Estimated
- Gray: Unknown

#### Cheap Area Hints
Visual overlay showing density of affordable restaurants in different areas.

### 9. TTC Integration
- Database of 50+ Toronto subway stations
- Walking time calculations (80m/min)
- Proximity-based filtering
- Station name display for nearby restaurants

### 10. Restaurant Details
- Full restaurant information
- Menu items with images
- "View Full Menu" / "Find Menu" web links
- Google Maps directions integration
- Price confidence indicators

### 11. Offline & Low-Data Mode

#### Features
- Automatic caching of recently viewed restaurants
- Thumbnail image caching (WiFi only by default)
- Offline indicator banner
- Graceful fallback to cached data
- Data usage settings

#### Cache Configuration
```kotlin
const val MAX_CACHE_AGE_DAYS = 7
const val MAX_CACHE_SIZE_MB = 50
const val MAX_CACHED_RESTAURANTS = 200
const val THUMBNAIL_SIZE = 200
```

### 12. Price Confidence Indicators
Shown in restaurant detail screen:
| Data Freshness | Label | Supporting Text |
|----------------|-------|-----------------|
| LIVE | Price verified | Checked just now |
| RECENT | Price verified | Updated within the hour |
| CACHED | Price may vary | Last checked a while ago |
| UNKNOWN | Price unverified | Confirm before ordering |

### 13. Time-Aware Ranking
- **Lunch hours (11 AM - 2 PM):** TTC proximity weighted 45%, rating 20%
- **Other times:** Default weights (TTC 30%, rating 30%)
- Prioritizes speed and convenience during lunch

### 14. Monetization
- Optional "Thank the Developer" tips
- Google Play Billing integration
- Three tiers: Small ($1.99), Medium ($4.99), Large ($9.99)

---

## Data Models

### Restaurant
```kotlin
data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val priceLevel: Int,              // 0-4 ($, $$, $$$, $$$$)
    val rating: Float,                // 0-5
    val distance: Float,              // miles
    val location: LatLng,
    val address: String,
    val imageUrl: String?,            // Google Places photo URL
    val isSponsored: Boolean,
    val hasStudentDiscount: Boolean,
    val nearTTC: Boolean,
    val averagePrice: Float?,         // estimated meal cost CAD
    val priceSource: PriceSource,     // API_VERIFIED, ESTIMATED, UNKNOWN
    val isOpenNow: Boolean?,          // null = unknown
    val ttcWalkMinutes: Int?,
    val nearestStation: String?,
    val dataFreshness: DataFreshness,
    val lastVerified: Long?,
    val isFavorite: Boolean           // user marked as favorite
)
```

### Deal
```kotlin
@Entity(tableName = "deals")
data class Deal(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val title: String,
    val description: String?,
    val originalPrice: Float?,
    val dealPrice: Float,
    val dealType: DealType,
    val source: DealSource,
    val validDays: Int,               // Bitmask
    val startTime: String?,           // "HH:mm"
    val endTime: String?,             // "HH:mm"
    val validUntil: Long?,            // Timestamp
    val upvotes: Int,
    val downvotes: Int,
    val reportCount: Int
)
```

### Collection
```kotlin
@Entity(tableName = "collections")
data class Collection(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isSystem: Boolean,
    val sortOrder: Int,
    val createdAt: Long
)
```

### RankedRestaurant
```kotlin
data class RankedRestaurant(
    val restaurant: Restaurant,
    val score: Float,
    val reasons: List<RecommendationReason>,
    val explanation: String,          // "Open now, verified cheap"
    val trustLabel: String            // "Live data" / "Cached 2h ago"
)
```

---

## Database (Room)

### Tables

| Table | Purpose | Key Indexes |
|-------|---------|-------------|
| cached_restaurants | Offline restaurant cache | lat/lng, price, rating, nearTTC |
| collections | User collections | sortOrder |
| collection_restaurants | Collection membership | restaurantId, addedAt |
| view_history | View tracking | restaurantId, viewedAt |
| deals | Active deals | restaurantId, price, validUntil, validDays |
| user_settings | App preferences | - |

### DAOs
- `CacheDao` - Cached restaurant operations
- `CollectionDao` - Collection CRUD, membership
- `ViewHistoryDao` - View tracking
- `DealDao` - Deal queries, voting
- `SettingsDao` - User preferences

---

## Services

### PlacesService
Integrates with Google Places API (New) for restaurant data.

**Key Features:**
- POST requests to `places.googleapis.com/v1/places:searchNearby`
- Field masking for efficient queries
- Automatic cuisine type mapping
- Price level conversion
- TTC proximity calculation
- Opening hours parsing

### RestaurantRanker
Multi-factor ranking algorithm with time-aware optimization.

**Default Scoring Weights:**
- Value (40%) - Lower price = higher score
- TTC Proximity (30%) - Closer to station = higher score
- Rating (30%) - Higher rating = higher score

**Lunch Hour Weights (11 AM - 2 PM):**
- Value (35%) - Slightly reduced
- TTC Proximity (45%) - Prioritized for quick access
- Rating (20%) - Reduced (reliability over novelty)

**Additional Modifiers:**
- Favorite boost: 1.15x (if open and under $15)
- Never ranks UNKNOWN data freshness as #1

### ReasonGenerator
Generates recommendation reasons and explanations.

```kotlin
object ReasonGenerator {
    fun generateReasons(restaurant, filterState, searchQuery): List<RecommendationReason>
    fun generateExplanation(reasons): String
    fun rankWithReasons(restaurant, score, filterState): RankedRestaurant
}
```

### OfflineManager
Manages connectivity monitoring and caching.

**Features:**
- Real-time connectivity state via NetworkCallback
- Restaurant caching with location context
- Thumbnail caching (WiFi only)
- Cache cleanup and statistics
- Proper resource cleanup with `release()` method

### DealRepository
Manages deal operations.

**Features:**
- Active deals by day/time
- Deal voting (upvote/downvote)
- User deal submission with validation
- Expiring deal notifications
- Automatic cleanup of expired deals

---

## UI Components

### HomeScreen
Main application screen with:
- Search field (OutlinedTextField)
- Filter bar (horizontal chips)
- Restaurant list (LazyColumn) with photo cards
- Favorite heart icon on each restaurant card
- AI recommendation card ("Quick Pick")
- View mode toggle (List/Map)
- Three-dot menu with tip option
- First-time user tip banner

### FilterBar
Horizontal scrollable filter chips:
- Open Now, Under $15, Student Discount, Near TTC
- Clear all button
- TTC filter hidden outside Toronto

### RestaurantDetailScreen
Detail view with:
- Restaurant header (cuisine, rating, price)
- Price confidence indicator
- Action buttons (Menu, Directions)
- Dish list with images and price badges

### DealCard
Deal display with:
- Title and restaurant name
- Price with savings calculation
- Time remaining badge
- Source indicator
- Voting controls (for user-submitted)

### CollectionPicker
Dialog for managing collections:
- System and custom collections
- Check/uncheck to add/remove restaurants
- Create new collection option

### OfflineBanner
Displays when offline with cached data count.

---

## Performance Optimizations

1. **Database Indexes**
   - Location queries: lat/lng composite index
   - Price filtering: averagePrice index
   - Sorting: rating, lastAccessedAt indexes
   - Deal queries: restaurantId, dealPrice, validUntil indexes

2. **Map Rendering**
   - Cached marker icons (standard, sponsored, cluster)
   - Disabled 3D buildings and indoor maps
   - Cluster rendering with icon reuse

3. **TTC Lookups**
   - LRU cache (100 entries, ~10m precision)
   - Bounding box pre-filtering

4. **Search**
   - 800ms debounce on AI queries
   - Sample data shown immediately while loading

5. **List Performance**
   - LazyColumn with stable keys
   - contentType for efficient view recycling
   - derivedStateOf for filter calculations

6. **Image Loading**
   - Size-constrained requests (160px thumbnails)
   - Memory and disk caching with stable keys
   - Crossfade animations

7. **Network**
   - 10s connect / 15s read timeouts
   - Retry on connection failure
   - WiFi-only thumbnail caching

8. **Memory Management**
   - Proper cleanup of network callbacks
   - Cache size limits

---

## External APIs

| API | Purpose |
|-----|---------|
| Google Places API (New) | Restaurant search, details, photos |
| Google Maps SDK | Map rendering, directions |
| Vertex AI (Gemini) | AI search and recommendations |
| Vertex AI (Imagen) | Logo generation |
| Google Play Billing | In-app tips |
| Fused Location | GPS location |

---

## Configuration

### API Keys Required
1. **Google Maps API Key** - in `local.properties` as `MAPS_API_KEY`
2. **Gemini API Key** - in `local.properties` as `GEMINI_API_KEY`
3. **Service Account JSON** - in `assets/` folder for Vertex AI OAuth

### Build Variants
- **Debug** - Development with test location support
- **Release** - ProGuard enabled, signed APK

---

## Key Dependencies

```toml
# Compose
composeBom = "2024.09.00"

# Google Services
playServicesMaps = "18.2.0"
playServicesLocation = "21.2.0"
mapsCompose = "4.3.3"
places = "3.3.0"

# AI
generativeAi = "0.7.0"
googleAuthOauth2 = "1.23.0"

# Database
room = "2.6.1"

# Billing
billing = "7.0.0"

# Networking
okhttp = "4.12.0"
gson = "2.10.1"

# Image Loading
coil = "2.5.0"
```

---

## Testing

### Unit Tests (470+ tests)
- `RestaurantTest` - Restaurant model tests
- `RestaurantRankerTest` - Ranking algorithm tests
- `ReasonGeneratorTest` - Reason generation tests
- `FilterContractTest` - Filter enforcement tests
- `DealTimeHelperTest` - Day bitmask tests
- `DataFreshnessTest` - Data freshness tests
- `PriceSourceTest` - Price source tests
- `PriceFilterModeTest` - Filter mode tests

### Running Tests
```bash
./gradlew test                     # All unit tests
./gradlew testDebugUnitTest        # Debug variant only
./gradlew connectedAndroidTest     # Instrumented tests
```

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with core features |
| 1.1.0 | Added favorites, restaurant photos, price confidence indicators, time-aware ranking |
| 1.2.0 | Added AI Trust & Transparency, Deals system, Collections, Offline mode, Map enhancements, Performance optimizations |

---

*Last Updated: January 27, 2026*

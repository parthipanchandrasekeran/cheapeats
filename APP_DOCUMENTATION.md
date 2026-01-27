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
│   ├── FavoritesManager.kt            # Favorites persistence
│   ├── PlacesService.kt               # Google Places API
│   ├── VertexAiService.kt             # AI recommendations
│   ├── TransitHelper.kt               # TTC station data
│   └── BillingService.kt              # In-app purchases
├── database/                          # Room persistence layer
└── ui/                                # Jetpack Compose UI
    ├── home/                          # Main screen with list
    ├── filter/                        # Filter components
    ├── detail/                        # Restaurant detail
    ├── map/                           # Map view
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

### 4. Dual View Modes
- **List View**: Scrollable restaurant cards with search and filters
- **Map View**: Google Map with clustered markers (gold = sponsored, red = standard)

### 5. TTC Integration
- Database of 50+ Toronto subway stations
- Walking time calculations (80m/min)
- Proximity-based filtering
- Station name display for nearby restaurants

### 6. Restaurant Details
- Full restaurant information
- Menu items (sample data)
- "View Full Menu" / "Find Menu" web links
- Google Maps directions integration

### 7. Favorites
- Heart icon on restaurant cards to toggle favorites
- Persisted via SharedPreferences
- Favorites receive 15% ranking boost (when open and under $15)
- AI recommendations prefer favorites during busy times

### 8. Price Confidence Indicators
Shown in restaurant detail screen only:
| Data Freshness | Label | Supporting Text |
|----------------|-------|-----------------|
| LIVE | Price verified | Checked just now |
| RECENT | Price verified | Updated within the hour |
| CACHED | Price may vary | Last checked a while ago |
| UNKNOWN | Price unverified | Confirm before ordering |

### 9. Time-Aware Ranking
- **Lunch hours (11 AM - 2 PM):** TTC proximity weighted 45%, rating 20%
- **Other times:** Default weights (TTC 30%, rating 30%)
- Prioritizes speed and convenience during lunch

### 10. Monetization
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
    val isOpenNow: Boolean?,          // null = unknown
    val ttcWalkMinutes: Int?,
    val nearestStation: String?,
    val dataFreshness: DataFreshness,
    val lastVerified: Long?,
    val isFavorite: Boolean           // user marked as favorite
)
```

### DataFreshness
```kotlin
enum class DataFreshness {
    LIVE,      // Real-time API data
    RECENT,    // < 1 hour old
    CACHED,    // > 1 hour old
    UNKNOWN    // No timestamp
}
```

### Dish
```kotlin
data class Dish(
    val id: String,
    val name: String,
    val description: String,
    val price: Float,
    val category: String,
    val isVegetarian: Boolean,
    val isSpicy: Boolean
)
```

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

### VertexAiService
Powers AI features using Google Vertex AI.

**Capabilities:**
- Semantic restaurant search
- Personalized recommendations
- Logo generation (Imagen 3.0)

**AI System Prompt Includes:**
- Toronto-focused context
- Budget-conscious priorities ($15 CAD limit)
- TTC proximity awareness
- Time-aware suggestions (lunch: speed, evening: portions)
- No-hype, practical language

### BillingService
Handles in-app purchases via Google Play Billing.

**Products:**
- `tip_small` - $1.99
- `tip_medium` - $4.99
- `tip_large` - $9.99

### TransitHelper
Toronto TTC subway network database.

**Features:**
- 50+ station coordinates
- `isTransitAccessible()` - within 500m of station
- `findNearestStation()` - returns station and distance
- `walkingTimeMinutes()` - converts meters to walk time
- LRU cache for performance

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

### FavoritesManager
Manages user favorites with SharedPreferences.

**Features:**
- Reactive StateFlow for UI updates
- Toggle, add, remove operations
- Applies favorite status to restaurant lists

---

## Database (Room)

### Tables

**restaurants**
- Primary restaurant data
- Indexed by: place_id, price_level, near_ttc, coordinates

**daily_specials**
- Time-limited restaurant offers
- Foreign key to restaurants
- Indexed by: day_of_week, is_active

### DAOs
- `RestaurantDao` - CRUD operations, filtered queries, search
- `DailySpecialDao` - Daily specials by day, category, price

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
- Price confidence indicator (subtle text based on data freshness)
- Action buttons (Menu, Directions)
- Dish list with price badges

### MapScreen
Google Map with:
- Clustered markers
- Color coding (gold/sponsored, red/standard)
- Legend overlay
- Info overlay (count, status)
- Zoom/location controls

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

## Performance Optimizations

1. **Map Rendering**
   - Cached marker icons (standard, sponsored, cluster)
   - Disabled 3D buildings and indoor maps
   - Cluster rendering with icon reuse

2. **TTC Lookups**
   - LRU cache (100 entries, ~10m precision)
   - Bounding box pre-filtering

3. **Search**
   - 800ms debounce on AI queries
   - Sample data shown immediately while loading

4. **Network**
   - 10s connect / 15s read timeouts
   - Retry on connection failure

---

## User Experience

### First Launch
- Location permission request
- First-time tip: "CheapEats works best near TTC stations during lunch hours"
- Sample data shown immediately

### Search Flow
1. User types query
2. 800ms debounce
3. AI processes semantic intent
4. Results filtered and ranked
5. Optional AI recommendation card

### Filter Flow
1. Tap filter chip to toggle
2. Multiple filters use AND logic
3. Results update instantly
4. Clear all resets filters

---

## Future Considerations

- Real menu data integration
- User history tracking
- Push notifications for deals
- Social sharing
- Multi-city expansion
- Review integration

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with core features |
| 1.1.0 | Added favorites, restaurant photos, price confidence indicators, time-aware ranking |

---

*Last Updated: January 27, 2026*

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CheapEats is a native Android application built with Kotlin and Jetpack Compose. It helps Toronto users find affordable dining options under $15 CAD with AI-powered recommendations, TTC transit integration, and offline support.

- **Package**: `com.parthipan.cheapeats`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Kotlin**: 2.0.21 with Compose plugin

## Build Commands

```bash
# Build
./gradlew build                    # Full build
./gradlew assembleDebug            # Debug APK
./gradlew assembleRelease          # Release APK
./gradlew clean                    # Clean build artifacts

# Test
./gradlew test                     # Run unit tests
./gradlew testDebugUnitTest        # Run specific variant unit tests
./gradlew connectedAndroidTest     # Run instrumented tests (requires device/emulator)

# Single test class
./gradlew test --tests "com.parthipan.cheapeats.ExampleUnitTest"
```

## Architecture

MVVM architecture with Room database and Jetpack Compose UI:

```
app/src/main/java/com/parthipan/cheapeats/
├── MainActivity.kt                    # Entry point
├── data/                              # Business logic & data models
│   ├── Restaurant.kt                  # Restaurant data model
│   ├── RestaurantRanker.kt            # Multi-factor ranking algorithm
│   ├── ReasonGenerator.kt             # "Why this pick?" reasons
│   ├── FilterContract.kt              # Hard filter enforcement
│   ├── RecommendationReason.kt        # Recommendation reason enum
│   ├── PlacesService.kt               # Google Places API integration
│   ├── VertexAiService.kt             # AI recommendations
│   ├── TransitHelper.kt               # TTC station database
│   ├── FavoritesManager.kt            # Favorites persistence
│   ├── BillingService.kt              # In-app purchases
│   ├── cache/                         # Offline caching
│   ├── deals/                         # Deals & daily specials
│   ├── favorites/                     # Collections system
│   ├── map/                           # Map enhancements
│   └── settings/                      # User settings
├── data/database/                     # Room persistence
│   ├── AppDatabase.kt                 # Main database (6 entities)
│   ├── CacheDao.kt                    # Offline cache operations
│   ├── DealDao.kt                     # Deal operations
│   ├── CollectionDao.kt               # Collection operations
│   ├── ViewHistoryDao.kt              # View tracking
│   └── SettingsDao.kt                 # Settings operations
└── ui/                                # Jetpack Compose UI
    ├── home/HomeScreen.kt             # Main screen
    ├── filter/                        # Filter bar & view model
    ├── detail/                        # Restaurant detail
    ├── map/                           # Map view & overlays
    ├── deals/                         # Deal cards
    ├── favorites/                     # Collection picker
    ├── components/                    # Shared components
    └── theme/                         # Material 3 theming
```

## Key Features

- **Restaurant Discovery**: Google Places API with 1.5km radius search
- **AI Search**: Vertex AI (Gemini) semantic search
- **Smart Filtering**: Open Now, Under $15, Student Discount, Near TTC
- **AI Trust**: "Why this pick?" reason chips, filter contract enforcement
- **Deals System**: Day bitmask scheduling, voting, user submissions
- **Collections**: System collections + custom, many-to-many relationships
- **Offline Mode**: Auto-caching, WiFi-only thumbnails, graceful fallback
- **Time-Aware Ranking**: Lunch hour optimization (11 AM - 2 PM)
- **TTC Integration**: 50+ stations, walking time calculations

## Key Dependencies

- Compose BOM 2024.09.00
- Room 2.6.1 for database
- Google Maps SDK + Places API
- Vertex AI (Gemini 1.5 Flash)
- Coil 2.5.0 for images
- Google Play Billing 7.0.0

## Development Notes

- Uses Gradle Kotlin DSL (`build.gradle.kts`)
- Version catalog in `gradle/libs.versions.toml`
- Java 11 source/target compatibility
- Code style: Official Kotlin style
- ProGuard minification disabled for release builds
- Database indexes on frequently queried columns
- LazyColumn uses contentType for efficient recycling

## Testing

470+ unit tests covering:
- Restaurant model and ranking
- Filter contract enforcement
- Reason generation
- Deal time helper (day bitmasks)
- Data freshness and price source

Run with `./gradlew test`

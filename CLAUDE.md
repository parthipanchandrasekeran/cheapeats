# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CheapEats is a native Android application built with Kotlin and Jetpack Compose. It uses Material Design 3 with support for dynamic colors on Android 12+.

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

Currently a simple single-module structure in early development:

```
app/src/main/java/com/parthipan/cheapeats/
├── MainActivity.kt          # Entry point, sets up Compose content
└── ui/theme/
    ├── Color.kt             # Color palette definitions
    ├── Theme.kt             # Material3 theme configuration (light/dark/dynamic)
    └── Type.kt              # Typography styles
```

- **UI Layer**: Jetpack Compose with Material3
- **Theme**: Supports light/dark mode with dynamic colors (Android 12+), fallback to Purple/Pink scheme
- **Edge-to-edge**: Enabled via `enableEdgeToEdge()`

## Key Dependencies

- Compose BOM 2024.09.00 for coordinated Compose library versions
- AndroidX Core KTX, Lifecycle, Activity Compose
- Testing: JUnit 4, Espresso, Compose UI Test

## Development Notes

- Uses Gradle Kotlin DSL (`build.gradle.kts`)
- Version catalog in `gradle/libs.versions.toml` for centralized dependency management
- Java 11 source/target compatibility
- Code style: Official Kotlin style (`kotlin.code.style=official` in gradle.properties)
- ProGuard minification disabled for release builds

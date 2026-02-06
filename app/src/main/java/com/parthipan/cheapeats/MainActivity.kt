package com.parthipan.cheapeats

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.parthipan.cheapeats.data.PlacesService
import com.parthipan.cheapeats.data.VertexAiService
import com.parthipan.cheapeats.data.database.AppDatabase
import com.parthipan.cheapeats.data.settings.ThemeMode
import com.parthipan.cheapeats.data.settings.UserSettings
import com.parthipan.cheapeats.ui.home.HomeScreen
import com.parthipan.cheapeats.ui.onboarding.OnboardingScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme

class MainActivity : ComponentActivity() {

    private val vertexAiService: VertexAiService by lazy {
        VertexAiService(applicationContext)
    }

    private val placesService: PlacesService by lazy {
        // Uses the same Maps API key for Places
        PlacesService(applicationContext, BuildConfig.MAPS_API_KEY)
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe theme settings from database
            val settings by database.settingsDao().getSettings()
                .collectAsState(initial = UserSettings())

            // Determine dark theme based on user preference
            val darkTheme = when (settings?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CheapEatsTheme(darkTheme = darkTheme) {
                val prefs = remember {
                    getSharedPreferences("cheapeats_prefs", Context.MODE_PRIVATE)
                }
                val shouldShowOnboarding = remember {
                    !prefs.getBoolean("onboarding_completed", false) &&
                        prefs.getBoolean("is_first_launch", true)
                }
                var showOnboarding by remember { mutableStateOf(shouldShowOnboarding) }

                if (showOnboarding) {
                    OnboardingScreen(
                        onComplete = {
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                            showOnboarding = false
                        }
                    )
                } else {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        vertexAiService = vertexAiService,
                        placesService = placesService,
                        database = database
                    )
                }
            }
        }
    }
}

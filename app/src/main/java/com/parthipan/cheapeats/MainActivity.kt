package com.parthipan.cheapeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.parthipan.cheapeats.data.PlacesService
import com.parthipan.cheapeats.data.VertexAiService
import com.parthipan.cheapeats.ui.home.HomeScreen
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme

class MainActivity : ComponentActivity() {

    private val vertexAiService: VertexAiService by lazy {
        VertexAiService(applicationContext)
    }

    private val placesService: PlacesService by lazy {
        // Uses the same Maps API key for Places
        PlacesService(applicationContext, BuildConfig.MAPS_API_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheapEatsTheme {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    vertexAiService = vertexAiService,
                    placesService = placesService
                )
            }
        }
    }
}

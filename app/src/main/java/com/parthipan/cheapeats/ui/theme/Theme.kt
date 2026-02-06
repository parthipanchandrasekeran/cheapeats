package com.parthipan.cheapeats.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class TonalSurfaces(
    val surface1: Color = Color.Unspecified,
    val surface2: Color = Color.Unspecified,
    val surface3: Color = Color.Unspecified
)

val LocalTonalSurfaces = staticCompositionLocalOf { TonalSurfaces() }

private val DarkColorScheme = darkColorScheme(
    primary = Orange80,
    onPrimary = OnOrangeDark,
    secondary = Amber80,
    onSecondary = OnAmberDark,
    tertiary = Amber80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = SurfaceTonalDark1,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Orange40,
    onPrimary = OnOrangeLight,
    secondary = Amber40,
    onSecondary = OnAmberLight,
    tertiary = Amber40,
    background = WarmWhite,
    surface = Cream,
    surfaceVariant = SurfaceTonalLight1,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val LightTonalSurfaces = TonalSurfaces(
    surface1 = SurfaceTonalLight1,
    surface2 = SurfaceTonalLight2,
    surface3 = SurfaceTonalLight3
)

private val DarkTonalSurfaces = TonalSurfaces(
    surface1 = SurfaceTonalDark1,
    surface2 = SurfaceTonalDark2,
    surface3 = SurfaceTonalDark3
)

@Composable
fun CheapEatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val tonalSurfaces = if (darkTheme) DarkTonalSurfaces else LightTonalSurfaces

    CompositionLocalProvider(LocalTonalSurfaces provides tonalSurfaces) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

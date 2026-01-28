package com.parthipan.cheapeats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.settings.CacheStats
import com.parthipan.cheapeats.data.settings.ThemeMode
import com.parthipan.cheapeats.data.settings.UserSettings

/**
 * Settings bottom sheet containing all app settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: UserSettings,
    cacheStats: CacheStats,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSettingsChange: (UserSettings) -> Unit,
    onClearCache: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Appearance Section (Theme Mode)
            AppearanceSettingsSection(
                currentThemeMode = settings.themeMode,
                onThemeModeChange = onThemeModeChange
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Data & Storage Section
            DataSettingsSection(
                settings = settings,
                cacheStats = cacheStats,
                onSettingsChange = onSettingsChange,
                onClearCache = onClearCache
            )
        }
    }
}

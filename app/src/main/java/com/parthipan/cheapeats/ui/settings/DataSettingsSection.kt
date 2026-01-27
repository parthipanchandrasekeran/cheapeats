package com.parthipan.cheapeats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.settings.CacheStats
import com.parthipan.cheapeats.data.settings.UserSettings

/**
 * Settings section for data and storage options.
 */
@Composable
fun DataSettingsSection(
    settings: UserSettings,
    cacheStats: CacheStats,
    onSettingsChange: (UserSettings) -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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

/**
 * A settings row with a switch toggle.
 */
@Composable
fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

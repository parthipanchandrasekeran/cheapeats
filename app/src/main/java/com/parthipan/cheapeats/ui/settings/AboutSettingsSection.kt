package com.parthipan.cheapeats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.BuildConfig

/**
 * Settings section for app info and update checking.
 */
@Composable
fun AboutSettingsSection(
    onCheckForUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        ListItem(
            headlineContent = { Text("Check for Update") },
            supportingContent = { Text("v${BuildConfig.VERSION_NAME}") },
            modifier = Modifier.clickable { onCheckForUpdate() }
        )
    }
}

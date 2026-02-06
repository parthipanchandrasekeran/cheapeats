package com.parthipan.cheapeats.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.PriceSource
import com.parthipan.cheapeats.data.Restaurant

/**
 * Info card shown when a restaurant marker is tapped on the map.
 */
@Composable
fun MapInfoCard(
    restaurant: Restaurant,
    onNavigateClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price with confidence
                PriceConfidenceBadge(restaurant = restaurant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick info row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Open status
                OpenStatusBadge(isOpen = restaurant.isOpenNow)

                // Distance
                restaurant.distance?.let { dist ->
                    Text(
                        text = "${String.format("%.1f", dist)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // TTC walk time
                restaurant.ttcWalkMinutes?.let { mins ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFDA291C)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${mins}min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate")
                }

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Menu")
                }
            }
        }
    }
}

@Composable
private fun PriceConfidenceBadge(restaurant: Restaurant) {
    Column(horizontalAlignment = Alignment.End) {
        // Price
        Text(
            text = restaurant.averagePrice?.let {
                "~$${String.format("%.0f", it)}"
            } ?: restaurant.pricePoint,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (restaurant.isVerifiedUnder15) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        // Confidence label
        if (restaurant.averagePrice != null) {
            Text(
                text = restaurant.priceConfidenceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = when (restaurant.priceSource) {
                    PriceSource.API_VERIFIED -> Color(0xFF4CAF50)
                    PriceSource.ESTIMATED -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun OpenStatusBadge(isOpen: Boolean?) {
    val (text, color) = when (isOpen) {
        true -> "Open" to Color(0xFF4CAF50)
        false -> "Closed" to Color(0xFFF44336)
        null -> "Hours unknown" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

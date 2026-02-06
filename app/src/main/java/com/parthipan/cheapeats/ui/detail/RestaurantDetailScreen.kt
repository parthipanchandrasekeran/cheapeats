package com.parthipan.cheapeats.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.parthipan.cheapeats.data.DataFreshness
import com.parthipan.cheapeats.data.Restaurant

private val HeroExpandedHeight = 220.dp
private val HeroCollapsedHeight = 56.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val heroExpandedPx = with(density) { HeroExpandedHeight.toPx() }
    val heroCollapsedPx = with(density) { HeroCollapsedHeight.toPx() }
    val heroRangePx = heroExpandedPx - heroCollapsedPx

    // How far collapsed we are: 0 = fully expanded, 1 = fully collapsed
    val collapsedFraction by remember {
        derivedStateOf {
            (scrollState.value / heroRangePx).coerceIn(0f, 1f)
        }
    }

    // Hero height shrinks as user scrolls
    val heroHeight by remember {
        derivedStateOf {
            with(density) {
                val heightPx = heroExpandedPx - scrollState.value.coerceAtMost(heroRangePx.toInt())
                heightPx.toDp()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero image section (shrinks with scroll)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroExpandedHeight)
            ) {
                // Letter placeholder fallback
                val placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = restaurant.name.take(1),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Actual hero image with parallax
                if (restaurant.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(restaurant.imageUrl)
                            .crossfade(300)
                            .size(600)
                            .memoryCacheKey("detail_${restaurant.id}")
                            .diskCacheKey("detail_${restaurant.id}")
                            .build(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = scrollState.value * 0.5f
                            }
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Name + rating overlaid on gradient (fades out as collapsed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .alpha(1f - collapsedFraction),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = restaurant.cuisine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    if (restaurant.rating > 0) {
                        Row(
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", restaurant.rating),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Info chips row
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open status chip
                if (restaurant.isOpenNow == true) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF4CAF50), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Now", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            labelColor = Color(0xFF2E7D32)
                        ),
                        border = null
                    )
                }

                // Price chip
                if (restaurant.averagePrice != null || restaurant.priceLevel > 0) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = if (restaurant.averagePrice != null) {
                                    "~\$${String.format("%.0f", restaurant.averagePrice)} CAD"
                                } else {
                                    restaurant.pricePoint
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null
                    )
                }

                // Distance chip
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = String.format("%.1f km", restaurant.distance),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )

                // TTC chip
                if (restaurant.nearTTC) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = restaurant.nearestStation ?: "Near TTC",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFFDA291C).copy(alpha = 0.1f),
                            labelColor = Color(0xFFDA291C)
                        ),
                        border = null
                    )
                }

                // Student discount chip
                if (restaurant.hasStudentDiscount) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text("Student Discount", style = MaterialTheme.typography.labelSmall)
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            labelColor = Color(0xFF7B1FA2)
                        ),
                        border = null
                    )
                }
            }

            // Address
            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Price confidence indicator
            if (restaurant.averagePrice != null) {
                val (confidenceText, supportingText) = when (restaurant.dataFreshness) {
                    DataFreshness.LIVE -> "Price verified" to "Checked just now"
                    DataFreshness.RECENT -> "Price verified" to "Updated within the hour"
                    DataFreshness.CACHED -> "Price may vary" to "Last checked a while ago"
                    DataFreshness.UNKNOWN -> "Price unverified" to "Confirm before ordering"
                }
                Text(
                    text = "$confidenceText · $supportingText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price information section
            RestaurantPriceSection(restaurant = restaurant)

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons section
            RestaurantActionButtons(restaurant = restaurant)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pinned top bar (fades in when hero is collapsed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroCollapsedHeight)
                .alpha(collapsedFraction)
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Space for the floating back button
                Spacer(modifier = Modifier.width(48.dp))
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Back button — always visible, floating over hero
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(4.dp)
                .align(Alignment.TopStart)
                .background(
                    color = Color.Black.copy(alpha = 0.4f * (1f - collapsedFraction)),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (collapsedFraction > 0.5f)
                    MaterialTheme.colorScheme.onSurface
                else
                    Color.White
            )
        }
    }
}

@Composable
private fun RestaurantPriceSection(restaurant: Restaurant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Price summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Average Price",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (restaurant.averagePrice != null) {
                        "~\$${String.format("%.0f", restaurant.averagePrice)} CAD"
                    } else {
                        restaurant.pricePoint
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        restaurant.averagePrice != null && restaurant.averagePrice < 15 -> "Budget-friendly meal"
                        restaurant.averagePrice != null && restaurant.averagePrice < 25 -> "Moderate pricing"
                        restaurant.averagePrice != null -> "Higher-end dining"
                        else -> "Price level: ${restaurant.pricePoint}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Price details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Price Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Price level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Price Level",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = restaurant.pricePoint,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f km away", restaurant.distance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // TTC proximity
                if (restaurant.nearTTC) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TTC Access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = restaurant.nearestStation ?: "Near subway",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Student discount
                if (restaurant.hasStudentDiscount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Student Discount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // Open status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (restaurant.isOpenNow == true) "Open Now" else "Closed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (restaurant.isOpenNow == true) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantActionButtons(restaurant: Restaurant) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // View Full Menu button
        if (restaurant.websiteUrl != null) {
            Button(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.websiteUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Full Menu")
            }
        } else {
            // Search for menu online
            Button(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    val searchQuery = "${restaurant.name} menu Toronto"
                    val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=${Uri.encode(searchQuery)}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Find Menu")
            }
        }

        // Directions button
        OutlinedButton(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                val uri = restaurant.googleMapsUrl
                    ?: "https://www.google.com/maps/search/?api=1&query=${Uri.encode(restaurant.address)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Directions")
        }

        // Share button
        IconButton(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                val priceText = if (restaurant.averagePrice != null) {
                    "~\$${String.format("%.0f", restaurant.averagePrice)} CAD"
                } else {
                    restaurant.pricePoint
                }
                val shareText = "Check out ${restaurant.name} — ${restaurant.cuisine} near ${restaurant.address}. $priceText. Found on CheapEats!"
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share restaurant"))
            }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

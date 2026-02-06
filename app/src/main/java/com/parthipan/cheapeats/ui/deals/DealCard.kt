package com.parthipan.cheapeats.ui.deals

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.deals.Deal
import com.parthipan.cheapeats.data.deals.DealSource
import com.parthipan.cheapeats.data.deals.DealTimeHelper

/**
 * Card displaying a deal with time remaining and voting options.
 */
@Composable
fun DealCard(
    deal: Deal,
    onVote: (Boolean) -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeRemaining = remember(deal) { DealTimeHelper.getTimeRemainingText(deal) }
    val isActive = remember(deal) { DealTimeHelper.isDealActiveNow(deal) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deal.restaurantName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price display
                Column(horizontalAlignment = Alignment.End) {
                    if (deal.originalPrice != null) {
                        Text(
                            text = "$${String.format("%.2f", deal.originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", deal.dealPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Description
            deal.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Time indicator
            if (timeRemaining != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TimeRemainingBadge(timeRemaining = timeRemaining)
            }

            // Source badge + voting
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source indicator
                SourceBadge(source = deal.source)

                // Voting for user-submitted deals
                if (deal.isUserSubmitted) {
                    VotingRow(
                        netVotes = deal.netVotes,
                        onUpvote = { onVote(true) },
                        onDownvote = { onVote(false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRemainingBadge(timeRemaining: String) {
    Surface(
        color = Color(0xFFFF9800).copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeRemaining,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun SourceBadge(source: DealSource) {
    val text = when (source) {
        DealSource.OFFICIAL -> "Official"
        DealSource.VERIFIED -> "Verified"
        DealSource.USER_SUBMITTED -> "User tip"
        DealSource.SCRAPED -> ""
    }

    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VotingRow(
    netVotes: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onUpvote,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Upvote",
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = if (netVotes >= 0) "+$netVotes" else "$netVotes",
            style = MaterialTheme.typography.labelSmall,
            color = when {
                netVotes > 0 -> Color(0xFF4CAF50)
                netVotes < 0 -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        IconButton(
            onClick = onDownvote,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Downvote",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Compact deal card for lists.
 */
@Composable
fun DealCardCompact(
    deal: Deal,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isActive = remember(deal) { DealTimeHelper.isDealActiveNow(deal) }
    val timeRemaining = remember(deal) { DealTimeHelper.getTimeRemainingText(deal) }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (timeRemaining != null) {
                    Text(
                        text = timeRemaining,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100)
                    )
                }
            }
            Text(
                text = "$${String.format("%.2f", deal.dealPrice)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

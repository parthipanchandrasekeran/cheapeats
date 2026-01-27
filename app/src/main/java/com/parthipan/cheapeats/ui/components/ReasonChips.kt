package com.parthipan.cheapeats.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parthipan.cheapeats.data.RecommendationReason

/**
 * Horizontal row of "Why this pick?" explanation chips.
 */
@Composable
fun ReasonChips(
    reasons: List<RecommendationReason>,
    modifier: Modifier = Modifier,
    onChipClick: ((RecommendationReason) -> Unit)? = null
) {
    if (reasons.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(reasons) { reason ->
            ReasonChip(
                reason = reason,
                onClick = { onChipClick?.invoke(reason) }
            )
        }
    }
}

/**
 * Single reason chip with icon and label.
 */
@Composable
fun ReasonChip(
    reason: RecommendationReason,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = getChipColors(reason)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = reason.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = reason.label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * Non-clickable version for display only.
 */
@Composable
fun ReasonChipDisplay(
    reason: RecommendationReason,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = getChipColors(reason)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = reason.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = reason.label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun getChipColors(reason: RecommendationReason): Pair<Color, Color> {
    return when (reason) {
        RecommendationReason.VERIFIED_UNDER_15 ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        RecommendationReason.OPEN_NOW ->
            Color(0xFF4CAF50).copy(alpha = 0.15f) to Color(0xFF2E7D32)

        RecommendationReason.NEAR_TTC ->
            Color(0xFFDA291C).copy(alpha = 0.15f) to Color(0xFFDA291C)

        RecommendationReason.HIGH_RATING ->
            Color(0xFFFFB300).copy(alpha = 0.15f) to Color(0xFFE65100)

        RecommendationReason.STUDENT_DISCOUNT ->
            Color(0xFF9C27B0).copy(alpha = 0.15f) to Color(0xFF7B1FA2)

        else ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

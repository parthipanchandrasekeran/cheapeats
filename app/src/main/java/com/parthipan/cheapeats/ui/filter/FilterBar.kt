package com.parthipan.cheapeats.ui.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parthipan.cheapeats.ui.theme.CheapEatsTheme

/**
 * Data class representing a filter chip configuration
 */
data class FilterChipData(
    val type: FilterType,
    val label: String,
    val isSelected: Boolean
)

/**
 * Horizontal filter bar with state-driven chips.
 *
 * Features:
 * - Three filter chips: 'Under $15', 'Student Discount', 'Near TTC'
 * - Horizontal scrolling for small screens
 * - Clear all button when filters are active
 * - Visual feedback for selected state
 * - TTC filter only shown when user is in Toronto area
 *
 * @param filterViewModel The ViewModel managing filter state
 * @param showTTCFilter Whether to show the "Near TTC" filter (should be false outside Toronto)
 * @param modifier Modifier for the composable
 */
@Composable
fun FilterBar(
    filterViewModel: FilterViewModel = viewModel(),
    showTTCFilter: Boolean = true,
    modifier: Modifier = Modifier
) {
    val filterState by filterViewModel.filterState.collectAsState()

    FilterBarContent(
        filterState = filterState,
        onFilterToggle = { filterType -> filterViewModel.toggleFilter(filterType) },
        onClearAll = { filterViewModel.clearAllFilters() },
        showTTCFilter = showTTCFilter,
        modifier = modifier
    )
}

/**
 * Stateless filter bar content - separated for easier testing and preview
 *
 * @param filterState Current filter state
 * @param onFilterToggle Callback when a filter is toggled
 * @param onClearAll Callback when clear all is pressed
 * @param showTTCFilter Whether to show the "Near TTC" filter option
 * @param modifier Modifier for the composable
 */
@Composable
fun FilterBarContent(
    filterState: FilterState,
    onFilterToggle: (FilterType) -> Unit,
    onClearAll: () -> Unit,
    showTTCFilter: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val chips = buildList {
        add(FilterChipData(
            type = FilterType.UNDER_15,
            label = "Under $15",
            isSelected = filterState.isUnder15Active
        ))
        add(FilterChipData(
            type = FilterType.STUDENT_DISCOUNT,
            label = "Student Discount",
            isSelected = filterState.isStudentDiscountActive
        ))
        // Only show TTC filter when user is in Toronto area
        if (showTTCFilter) {
            add(FilterChipData(
                type = FilterType.NEAR_TTC,
                label = "Near TTC",
                isSelected = filterState.isNearTTCActive
            ))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontally scrollable chips
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            chips.forEach { chip ->
                FilterChipItem(
                    label = chip.label,
                    isSelected = chip.isSelected,
                    onClick = { onFilterToggle(chip.type) }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Clear all button - only visible when filters are active
        if (filterState.hasActiveFilters) {
            IconButton(
                onClick = onClearAll,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear all filters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual filter chip with selected/unselected states
 */
@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

// Preview helpers

@Preview(showBackground = true)
@Composable
private fun FilterBarPreview() {
    CheapEatsTheme {
        FilterBarContent(
            filterState = FilterState(),
            onFilterToggle = {},
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterBarWithSelectionsPreview() {
    CheapEatsTheme {
        FilterBarContent(
            filterState = FilterState(
                isUnder15Active = true,
                isNearTTCActive = true
            ),
            onFilterToggle = {},
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterBarDarkPreview() {
    CheapEatsTheme(darkTheme = true) {
        FilterBarContent(
            filterState = FilterState(isStudentDiscountActive = true),
            onFilterToggle = {},
            onClearAll = {}
        )
    }
}

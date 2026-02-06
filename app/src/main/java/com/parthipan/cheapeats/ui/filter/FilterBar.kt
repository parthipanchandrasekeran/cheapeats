package com.parthipan.cheapeats.ui.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
        onPriceModeChange = { mode -> filterViewModel.setPriceFilterMode(mode) },
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
 * @param onPriceModeChange Callback when price filter mode changes
 * @param onClearAll Callback when clear all is pressed
 * @param showTTCFilter Whether to show the "Near TTC" filter option
 * @param modifier Modifier for the composable
 */
@Composable
fun FilterBarContent(
    filterState: FilterState,
    onFilterToggle: (FilterType) -> Unit,
    onPriceModeChange: (PriceFilterMode) -> Unit,
    onClearAll: () -> Unit,
    showTTCFilter: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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

            // Open Now chip
            FilterChipItem(
                label = "Open Now",
                isSelected = filterState.isOpenNowActive,
                onClick = { onFilterToggle(FilterType.OPEN_NOW) }
            )

            // Under $15 chip with mode dropdown
            Under15FilterChip(
                isActive = filterState.isUnder15Active,
                priceMode = filterState.priceFilterMode,
                onToggle = { onFilterToggle(FilterType.UNDER_15) },
                onModeChange = onPriceModeChange
            )

            // Student Discount chip
            FilterChipItem(
                label = "Student Discount",
                isSelected = filterState.isStudentDiscountActive,
                onClick = { onFilterToggle(FilterType.STUDENT_DISCOUNT) }
            )

            // Only show TTC filter when user is in Toronto area
            if (showTTCFilter) {
                FilterChipItem(
                    label = "Near TTC",
                    isSelected = filterState.isNearTTCActive,
                    onClick = { onFilterToggle(FilterType.NEAR_TTC) }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Clear all button with animated visibility
        AnimatedVisibility(
            visible = filterState.hasActiveFilters,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
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
 * Under $15 filter chip with Strict/Flexible mode dropdown
 */
@Composable
private fun Under15FilterChip(
    isActive: Boolean,
    priceMode: PriceFilterMode,
    onToggle: () -> Unit,
    onModeChange: (PriceFilterMode) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showModeMenu by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = isActive,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Under \$15",
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (isActive) {
                        Spacer(Modifier.width(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { showModeMenu = true }
                        ) {
                            Text(
                                text = if (priceMode == PriceFilterMode.STRICT) "Strict" else "Flex",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change mode",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            leadingIcon = if (isActive) {
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
            )
        )

        // Mode dropdown menu
        DropdownMenu(
            expanded = showModeMenu && isActive,
            onDismissRequest = { showModeMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text("Strict", fontWeight = FontWeight.Medium)
                        Text(
                            "Verified prices under \$15 only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    if (priceMode == PriceFilterMode.STRICT) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    onModeChange(PriceFilterMode.STRICT)
                    showModeMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Column {
                        Text("Flexible", fontWeight = FontWeight.Medium)
                        Text(
                            "Include estimated prices up to \$17",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    if (priceMode == PriceFilterMode.FLEXIBLE) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    onModeChange(PriceFilterMode.FLEXIBLE)
                    showModeMenu = false
                }
            )
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
    val hapticFeedback = LocalHapticFeedback.current

    FilterChip(
        selected = isSelected,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
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
            onPriceModeChange = {},
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
                priceFilterMode = PriceFilterMode.STRICT,
                isNearTTCActive = true,
                isOpenNowActive = true
            ),
            onFilterToggle = {},
            onPriceModeChange = {},
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterBarFlexibleModePreview() {
    CheapEatsTheme {
        FilterBarContent(
            filterState = FilterState(
                isUnder15Active = true,
                priceFilterMode = PriceFilterMode.FLEXIBLE
            ),
            onFilterToggle = {},
            onPriceModeChange = {},
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
            onPriceModeChange = {},
            onClearAll = {}
        )
    }
}

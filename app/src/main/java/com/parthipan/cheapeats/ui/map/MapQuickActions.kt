package com.parthipan.cheapeats.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.parthipan.cheapeats.data.Restaurant

/**
 * Quick action buttons for the map view.
 */
@Composable
fun MapQuickActions(
    userLocation: LatLng?,
    restaurants: List<Restaurant>,
    onClosestCheapClick: (Restaurant) -> Unit,
    onNearTTCClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val closestCheap = remember(userLocation, restaurants) {
        if (userLocation == null) return@remember null

        restaurants
            .filter { it.isVerifiedUnder15 || it.isFlexiblyUnder15 }
            .filter { it.isOpenNow == true }
            .minByOrNull { restaurant ->
                SphericalUtil.computeDistanceBetween(
                    userLocation,
                    restaurant.location
                )
            }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Closest Under $15 button
        FilledTonalButton(
            onClick = { closestCheap?.let { onClosestCheapClick(it) } },
            enabled = closestCheap != null,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Closest < \$15")
        }

        // Near TTC button
        OutlinedButton(
            onClick = onNearTTCClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Near TTC")
        }
    }
}

/**
 * Find the closest restaurant under $15 from the user's location.
 */
fun findClosestCheap(
    userLocation: LatLng,
    restaurants: List<Restaurant>
): Restaurant? {
    return restaurants
        .filter { it.isFlexiblyUnder15 }
        .filter { it.isOpenNow != false }
        .minByOrNull { restaurant ->
            SphericalUtil.computeDistanceBetween(
                userLocation,
                restaurant.location
            )
        }
}

package com.parthipan.cheapeats.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.parthipan.cheapeats.data.map.CheapAreaHint

/**
 * Overlay showing areas with clusters of cheap restaurants.
 * Only visible when zoomed out.
 */
@Composable
fun CheapAreaOverlay(
    hints: List<CheapAreaHint>,
    cameraPosition: CameraPositionState,
    onAreaClick: (CheapAreaHint) -> Unit = {}
) {
    // Only show when zoomed out enough
    val showHints = cameraPosition.position.zoom < 15f

    if (showHints) {
        hints.forEach { hint ->
            // Circle showing the area
            Circle(
                center = hint.center,
                radius = hint.radius.toDouble(),
                fillColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                strokeColor = Color(0xFF4CAF50).copy(alpha = 0.4f),
                strokeWidth = 2f
            )

            // Marker with count badge
            val icon = remember(hint.restaurantCount) {
                createCountBadge(hint.restaurantCount)
            }

            Marker(
                state = MarkerState(position = hint.center),
                icon = icon,
                title = hint.label,
                snippet = "Tap to zoom",
                onClick = {
                    onAreaClick(hint)
                    true
                }
            )
        }
    }
}

/**
 * Create a circular badge bitmap showing the count of restaurants.
 */
private fun createCountBadge(count: Int): BitmapDescriptor {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Green circle background
    val bgPaint = Paint().apply {
        color = Color(0xFF4CAF50).toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, bgPaint)

    // White border
    val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, borderPaint)

    // White text
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    canvas.drawText(
        count.toString(),
        size / 2f,
        size / 2f + 7,
        textPaint
    )

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Create a badge showing the label text.
 */
fun createLabelBadge(label: String): BitmapDescriptor {
    val width = 100
    val height = 32
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply {
        color = Color(0xFF4CAF50).toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 8f, 8f, bgPaint)

    // Text
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 12f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText(
        label,
        width / 2f,
        height / 2f + 4,
        textPaint
    )

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

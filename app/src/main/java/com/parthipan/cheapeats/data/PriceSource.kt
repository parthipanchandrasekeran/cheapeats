package com.parthipan.cheapeats.data

/**
 * Indicates the source and confidence level of a restaurant's price data.
 */
enum class PriceSource {
    API_VERIFIED,    // Direct from Google Places or menu API
    USER_REPORTED,   // Crowdsourced (future feature)
    ESTIMATED,       // Calculated from priceLevel
    UNKNOWN          // No data available
}

/**
 * Get user-facing label for price source.
 */
fun PriceSource.toDisplayLabel(): String = when (this) {
    PriceSource.API_VERIFIED -> "Verified"
    PriceSource.USER_REPORTED -> "Reported"
    PriceSource.ESTIMATED -> "Estimated"
    PriceSource.UNKNOWN -> ""
}

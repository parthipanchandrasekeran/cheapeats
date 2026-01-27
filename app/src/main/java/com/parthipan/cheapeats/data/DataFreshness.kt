package com.parthipan.cheapeats.data

/**
 * Represents the freshness level of restaurant data for trust labeling.
 * Used to indicate how recent/reliable the data is.
 */
enum class DataFreshness {
    /**
     * Live data from API, less than 5 minutes old
     */
    LIVE,

    /**
     * Recent data, less than 1 hour old
     */
    RECENT,

    /**
     * Cached data, more than 1 hour old
     */
    CACHED,

    /**
     * Unknown freshness, no timestamp available
     */
    UNKNOWN
}

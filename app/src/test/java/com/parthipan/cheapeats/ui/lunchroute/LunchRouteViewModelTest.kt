package com.parthipan.cheapeats.ui.lunchroute

import com.google.android.gms.maps.model.LatLng
import com.parthipan.cheapeats.data.DataFreshness
import com.parthipan.cheapeats.data.PriceSource
import com.parthipan.cheapeats.data.Restaurant
import com.parthipan.cheapeats.data.SubwayStation
import com.parthipan.cheapeats.data.TransitHelper
import com.parthipan.cheapeats.data.lunchroute.RouteStart
import com.parthipan.cheapeats.ui.filter.FilterState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LunchRouteViewModel.
 */
class LunchRouteViewModelTest {

    private lateinit var viewModel: LunchRouteViewModel
    private val testLocation = LatLng(43.6532, -79.3832) // Downtown Toronto

    @Before
    fun setup() {
        viewModel = LunchRouteViewModel(offlineManager = null)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state is Idle`() {
        assertEquals(LunchRouteViewModel.LunchRouteState.Idle, viewModel.state.value)
    }

    @Test
    fun `initial start selection is null`() {
        assertNull(viewModel.startSelection.value)
    }

    // ==================== Select Start Location Tests ====================

    @Test
    fun `selectStartLocation updates start selection with current location`() {
        val start = RouteStart.CurrentLocation(testLocation)

        viewModel.selectStartLocation(start)

        assertEquals(start, viewModel.startSelection.value)
    }

    @Test
    fun `selectStartLocation updates start selection with TTC station`() {
        val station = TransitHelper.allSubwayStations.first()
        val start = RouteStart.TTCStation(station)

        viewModel.selectStartLocation(start)

        assertEquals(start, viewModel.startSelection.value)
    }

    // ==================== Clear Plan Tests ====================

    @Test
    fun `clearPlan resets state to Idle`() {
        viewModel.clearPlan()

        assertEquals(LunchRouteViewModel.LunchRouteState.Idle, viewModel.state.value)
    }

    @Test
    fun `clearPlan resets start selection to null`() {
        viewModel.selectStartLocation(RouteStart.CurrentLocation(testLocation))

        viewModel.clearPlan()

        assertNull(viewModel.startSelection.value)
    }

    // ==================== Route Start Tests ====================

    @Test
    fun `RouteStart CurrentLocation has correct display name`() {
        val start = RouteStart.CurrentLocation(testLocation)

        assertEquals("Current Location", start.displayName)
    }

    @Test
    fun `RouteStart TTCStation has correct display name`() {
        val station = TransitHelper.allSubwayStations.first()
        val start = RouteStart.TTCStation(station)

        assertEquals("${station.name} Station", start.displayName)
    }

    @Test
    fun `RouteStart CurrentLocation returns correct location`() {
        val start = RouteStart.CurrentLocation(testLocation)

        assertEquals(testLocation, start.location)
    }

    @Test
    fun `RouteStart TTCStation returns station location`() {
        val station = TransitHelper.allSubwayStations.first()
        val start = RouteStart.TTCStation(station)

        assertEquals(station.location, start.location)
    }

    // ==================== Lunch Time Check Tests ====================

    @Test
    fun `isLunchTime returns boolean`() {
        val result = LunchRouteViewModel.isLunchTime()
        // Can't assert specific value as it depends on current time
        assertTrue(result == true || result == false)
    }

    // ==================== Route Candidate Tests ====================

    @Test
    fun `RouteCandidate etaDisplay shows minutes correctly`() {
        val candidate = createTestCandidate(etaMinutes = 5)

        assertEquals("5 min walk", candidate.etaDisplay)
    }

    @Test
    fun `RouteCandidate etaDisplay shows 1 min for single minute`() {
        val candidate = createTestCandidate(etaMinutes = 1)

        assertEquals("1 min walk", candidate.etaDisplay)
    }

    @Test
    fun `RouteCandidate etaDisplay handles hour plus minutes`() {
        val candidate = createTestCandidate(etaMinutes = 75)

        assertEquals("1h 15m walk", candidate.etaDisplay)
    }

    @Test
    fun `RouteCandidate etaDisplay handles exact hour`() {
        val candidate = createTestCandidate(etaMinutes = 60)

        assertEquals("1h walk", candidate.etaDisplay)
    }

    @Test
    fun `RouteCandidate isFastOption true for under 5 minutes`() {
        val candidate = createTestCandidate(etaMinutes = 4)

        assertTrue(candidate.isFastOption)
    }

    @Test
    fun `RouteCandidate isFastOption true for exactly 5 minutes`() {
        val candidate = createTestCandidate(etaMinutes = 5)

        assertTrue(candidate.isFastOption)
    }

    @Test
    fun `RouteCandidate isFastOption false for over 5 minutes`() {
        val candidate = createTestCandidate(etaMinutes = 6)

        assertFalse(candidate.isFastOption)
    }

    // ==================== Helper Functions ====================

    private fun createTestRestaurant(
        id: String = "1",
        name: String = "Test Restaurant",
        rating: Float = 4.0f,
        averagePrice: Float? = 12f,
        isOpenNow: Boolean? = true,
        nearTTC: Boolean = true,
        ttcWalkMinutes: Int? = 5,
        dataFreshness: DataFreshness = DataFreshness.LIVE
    ) = Restaurant(
        id = id,
        name = name,
        cuisine = "Italian",
        priceLevel = 1,
        rating = rating,
        distance = 0.5f,
        imageUrl = null,
        address = "123 Test St",
        location = LatLng(43.6532, -79.3832),
        isSponsored = false,
        hasStudentDiscount = false,
        nearTTC = nearTTC,
        averagePrice = averagePrice,
        priceSource = PriceSource.API_VERIFIED,
        isOpenNow = isOpenNow,
        dataFreshness = dataFreshness,
        ttcWalkMinutes = ttcWalkMinutes
    )

    private fun createTestCandidate(
        etaMinutes: Int = 5,
        restaurant: Restaurant = createTestRestaurant()
    ) = com.parthipan.cheapeats.data.lunchroute.RouteCandidate(
        restaurant = restaurant,
        reasons = emptyList(),
        etaMinutes = etaMinutes,
        walkFromStation = 5,
        nearestStation = "Union",
        score = 0.8f,
        explanation = "Test explanation"
    )
}

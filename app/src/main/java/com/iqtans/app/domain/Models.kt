package com.iqtans.app.domain

import java.time.LocalDate
import kotlin.math.roundToInt

enum class PriceTrust { VERIFIED_LIVE, DISCOVERED, DEMO }
enum class LiveSearchMode { LIVE, PARTIAL, UNCONFIGURED }

data class ReviewSource(
    val source: String,
    val score: Double,
    val scale: Int,
    val count: Int,
    val attribution: String? = null,
    val sourceUrl: String? = null
)
data class DiscoveryHint(val title: String, val url: String, val source: String, val description: String? = null)

data class DealOffer(
    val id: String, val source: String, val finalPrice: Int, val referencePrice: Int, val currency: String = "ر.س",
    val title: String, val method: String, val trust: PriceTrust, val matchPercent: Int, val lastChecked: String,
    val cancellation: String, val meal: String, val paymentLabel: String = "حسب العرض", val comparisonReason: String? = null,
    val conditions: List<String> = emptyList(), val steps: List<String> = emptyList(),
    val breakdown: List<Pair<String, Int>> = emptyList(), val cardRequirement: String? = null, val memberRequirement: String? = null,
    val cashbackLater: Int = 0, val bookingUrl: String? = null, val evidenceUrl: String? = null, val providerHotelId: String = "",
    val roomName: String? = null, val finalPriceExact: Double? = null, val referencePriceExact: Double? = null
) {
    val finalAmount: Double get() = finalPriceExact ?: finalPrice.toDouble()
    val referenceAmount: Double get() = referencePriceExact ?: referencePrice.toDouble()
    val savingsAmount: Double get() = (referenceAmount - finalAmount).coerceAtLeast(0.0)
    val savings: Int get() = savingsAmount.roundToInt()
    val savingsPercent: Int get() = if (referenceAmount <= 0.0) 0 else (savingsAmount * 100.0 / referenceAmount).roundToInt()
}

data class HotelDeal(
    val id: String, val name: String, val city: String, val area: String, val stars: Int, val rating: Double, val ratingCount: Int,
    val distanceLabel: String, val reviewSources: List<ReviewSource>, val offers: List<DealOffer>, val insight: String,
    val flexibilitySaving: Int = 0, val flexibleDateLabel: String? = null, val discoveries: List<DiscoveryHint> = emptyList(),
    val flexibilitySavingExact: Double? = null
) {
    val bestOffer: DealOffer get() = offers.minBy { it.finalAmount }
    val flexibilitySavingAmount: Double get() = flexibilitySavingExact ?: flexibilitySaving.toDouble()
}

data class UserPaymentCard(val id: String, val bank: String, val network: String, val tier: String, val isEnabled: Boolean = true)

data class SearchRequest(
    val city: String,
    val hotelQuery: String = "",
    val checkIn: String = LocalDate.now().plusDays(7).toString(),
    val checkOut: String = LocalDate.now().plusDays(10).toString(),
    val guests: Int = 2,
    val rooms: Int = 1,
    val flexibilityDays: Int = 1,
    val childrenAges: List<Int> = emptyList()
) {
    val adults: Int get() = guests
    val totalGuests: Int get() = guests + childrenAges.size
}

data class PriceWatch(
    val id: String,
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val checkIn: String,
    val checkOut: String,
    val guests: Int,
    val rooms: Int,
    val savedPrice: Double,
    val lastSeenPrice: Double,
    val bestSeenPrice: Double,
    val targetPrice: Double? = null,
    val lastNotifiedPrice: Double? = null,
    val currency: String,
    val savedAt: Long,
    val lastCheckedAt: Long = 0L,
    val childrenAges: List<Int> = emptyList()
) {
    fun toSearchRequest() = SearchRequest(
        city = city,
        hotelQuery = hotelName,
        checkIn = checkIn,
        checkOut = checkOut,
        guests = guests,
        rooms = rooms,
        flexibilityDays = 1,
        childrenAges = childrenAges
    )
}

data class SearchCoverage(
    val priceProvidersTotal: Int = 0,
    val priceProvidersConfigured: Int = 0,
    val priceProvidersWithResults: Int = 0,
    val liveOffers: Int = 0,
    val discoveredLeads: Int = 0,
    val configuredProviderNames: List<String> = emptyList(),
    val providerWarnings: List<String> = emptyList(),
    val reviewProvidersConfigured: Int = 0,
    val externalReviewSources: Int = 0
)

data class LiveSearchEnvelope(
    val hotels: List<HotelDeal>,
    val mode: LiveSearchMode,
    val warnings: List<String> = emptyList(),
    val generatedAt: String = "",
    val coverage: SearchCoverage = SearchCoverage()
)

data class OfferVerification(val available: Boolean, val expectedPrice: Double, val currentPrice: Double? = null, val changed: Boolean = false, val verifiedAt: String = "", val bookingUrl: String? = null, val reason: String? = null)

package com.iqtans.app.domain

enum class PriceTrust {
    VERIFIED_LIVE,
    DISCOVERED,
    DEMO
}

data class ReviewSource(
    val source: String,
    val score: Double,
    val scale: Int,
    val count: Int
)

data class DealOffer(
    val id: String,
    val source: String,
    val finalPrice: Int,
    val referencePrice: Int,
    val currency: String = "ر.س",
    val title: String,
    val method: String,
    val trust: PriceTrust,
    val matchPercent: Int,
    val lastChecked: String,
    val cancellation: String,
    val meal: String,
    val conditions: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val breakdown: List<Pair<String, Int>> = emptyList(),
    val cardRequirement: String? = null,
    val memberRequirement: String? = null,
    val cashbackLater: Int = 0
) {
    val savings: Int get() = (referencePrice - finalPrice).coerceAtLeast(0)
    val savingsPercent: Int get() = if (referencePrice <= 0) 0 else (savings * 100 / referencePrice)
}

data class HotelDeal(
    val id: String,
    val name: String,
    val city: String,
    val area: String,
    val stars: Int,
    val rating: Double,
    val ratingCount: Int,
    val distanceLabel: String,
    val reviewSources: List<ReviewSource>,
    val offers: List<DealOffer>,
    val insight: String,
    val flexibilitySaving: Int = 0,
    val flexibleDateLabel: String? = null
) {
    val bestOffer: DealOffer get() = offers.minBy { it.finalPrice }
}

data class SearchRequest(
    val city: String,
    val hotelQuery: String = "",
    val checkIn: String = "20 سبتمبر",
    val checkOut: String = "23 سبتمبر",
    val guests: Int = 2,
    val rooms: Int = 1,
    val flexibilityDays: Int = 1
)

data class UserPaymentCard(
    val id: String,
    val bank: String,
    val network: String,
    val tier: String,
    val isEnabled: Boolean = true
)

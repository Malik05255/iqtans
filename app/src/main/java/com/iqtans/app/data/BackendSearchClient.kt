package com.iqtans.app.data

import com.iqtans.app.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class BackendSearchClient(private val baseUrl: String) {
    suspend fun search(request: SearchRequest, cards: List<UserPaymentCard>): LiveSearchEnvelope = withContext(Dispatchers.IO) {
        parseEnvelope(post("/v1/search", searchPayload(request, cards)))
    }

    suspend fun verify(request: SearchRequest, cards: List<UserPaymentCard>, offer: DealOffer): OfferVerification = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("search", searchPayload(request.copy(flexibilityDays = 0), cards))
            put("provider", offer.source)
            put("providerHotelId", offer.providerHotelId)
            put("expectedPrice", offer.finalAmount)
            offer.roomName?.takeIf { it.isNotBlank() }?.let { put("roomName", it) }
        }
        val root = post("/v1/verify", payload)
        val fresh = root.optJSONObject("offer")
        OfferVerification(
            available = root.optBoolean("available", false),
            expectedPrice = root.optDouble("expectedPrice", offer.finalAmount),
            currentPrice = root.optNullableDouble("currentPrice"),
            changed = root.optBoolean("changed", false),
            verifiedAt = root.optString("verifiedAt"),
            bookingUrl = fresh?.optString("bookingUrl")?.takeIf { it.isNotBlank() },
            reason = root.optString("reason").takeIf { it.isNotBlank() }
        )
    }

    private fun searchPayload(request: SearchRequest, cards: List<UserPaymentCard>) = JSONObject().apply {
        put("city", request.city)
        if (request.hotelQuery.isNotBlank()) put("hotelName", request.hotelQuery)
        put("checkIn", request.checkIn)
        put("checkOut", request.checkOut)
        put("adults", request.guests)
        put("rooms", request.rooms)
        if (request.childrenAges.isNotEmpty()) {
            put("childrenAges", JSONArray().apply { request.childrenAges.forEach { age -> put(age) } })
        }
        put("bookerCountry", "sa")
        put("currency", "SAR")
        put("flexibilityDays", request.flexibilityDays)
        put("cards", JSONArray().apply {
            cards.filter { it.isEnabled }.forEach { card ->
                put(JSONObject().apply {
                    put("bank", card.bank)
                    put("network", card.network)
                    put("tier", card.tier)
                    put("country", "SA")
                })
            }
        })
    }

    private fun post(path: String, payload: JSONObject): JSONObject {
        val endpoint = URL(baseUrl.trimEnd('/') + path)
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15_000; readTimeout = 45_000; doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8"); setRequestProperty("Accept", "application/json")
        }
        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val text = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        connection.disconnect()
        if (code !in 200..299) throw IllegalStateException("خادم اقتنص أعاد HTTP $code${if (text.isNotBlank()) ": $text" else ""}")
        return JSONObject(text)
    }

    private fun parseEnvelope(root: JSONObject): LiveSearchEnvelope {
        val mode = when (root.optString("mode")) { "live" -> LiveSearchMode.LIVE; "partial" -> LiveSearchMode.PARTIAL; else -> LiveSearchMode.UNCONFIGURED }
        val warnings = mutableListOf<String>()
        root.optJSONArray("providers")?.forEachObject { p -> p.optString("warning").takeIf { it.isNotBlank() }?.let(warnings::add) }
        val hotels = mutableListOf<HotelDeal>()
        root.optJSONArray("hotels")?.forEachObject { hotel -> parseHotel(hotel)?.let(hotels::add) }
        val coverageObject = root.optJSONObject("coverage") ?: JSONObject()
        val coverage = SearchCoverage(
            priceProvidersTotal = coverageObject.optInt("priceProvidersTotal", 0),
            priceProvidersConfigured = coverageObject.optInt("priceProvidersConfigured", 0),
            priceProvidersWithResults = coverageObject.optInt("priceProvidersWithResults", 0),
            liveOffers = coverageObject.optInt("liveOffers", 0),
            discoveredLeads = coverageObject.optInt("discoveredLeads", 0)
        )
        return LiveSearchEnvelope(hotels, mode, warnings, root.optString("generatedAt"), coverage)
    }

    private fun parseHotel(hotel: JSONObject): HotelDeal? {
        val rawOffers = hotel.optJSONArray("offers") ?: return null
        if (rawOffers.length() == 0) return null
        val baselineExact = hotel.optDouble("baselinePrice", hotel.optDouble("bestPrice", 0.0))
        val offers = mutableListOf<DealOffer>(); var firstRaw: JSONObject? = null
        rawOffers.forEachObject { raw -> if (firstRaw == null) firstRaw = raw; offers += parseOffer(raw, baselineExact) }
        if (offers.isEmpty()) return null
        val first = firstRaw ?: JSONObject(); val discoveries = mutableListOf<DiscoveryHint>()
        hotel.optJSONArray("discoveries")?.forEachObject { d ->
            val url = d.optString("url")
            if (url.isNotBlank()) discoveries += DiscoveryHint(d.optString("title", "عرض مكتشف"), url, d.optString("source", "الويب"), d.optString("description").takeIf { it.isNotBlank() })
        }
        val flexible = hotel.optJSONObject("flexible")
        val flexibilitySavingExact = flexible?.optNullableDouble("savings") ?: 0.0
        val flexibleDateLabel = flexible?.let { val a = it.optString("checkIn"); val b = it.optString("checkOut"); if (a.isNotBlank() && b.isNotBlank()) "$a — $b" else null }
        val rating = first.optDouble("rating", 0.0); val ratingCount = first.optInt("ratingCount", 0); val provider = first.optString("provider", "مصدر حي")
        return HotelDeal(
            id = hotel.optString("key", hotel.optString("name")), name = hotel.optString("name", "فندق"), city = hotel.optString("city"), area = "نتيجة اقتنص الحية",
            stars = first.optInt("stars", 0).coerceIn(0, 5), rating = rating, ratingCount = ratingCount, distanceLabel = provider,
            reviewSources = if (rating > 0) listOf(ReviewSource(provider, rating, 10, ratingCount)) else emptyList(), offers = offers,
            insight = discoveries.firstOrNull()?.title ?: offers.first().method,
            flexibilitySaving = flexibilitySavingExact.roundToInt(), flexibleDateLabel = flexibleDateLabel, discoveries = discoveries, flexibilitySavingExact = flexibilitySavingExact
        )
    }

    private fun parseOffer(raw: JSONObject, hotelBaselineExact: Double): DealOffer {
        val totalExact = raw.optDouble("totalPrice", 0.0)
        val comparisonExact = raw.optDouble("comparisonPrice", hotelBaselineExact)
        val requirements = raw.optJSONArray("requirements").strings(); val breakdownObject = raw.optJSONObject("breakdown") ?: JSONObject()
        val breakdown = buildList {
            val room = breakdownObject.optDouble("room", 0.0).roundToInt(); val taxes = breakdownObject.optDouble("taxes", 0.0).roundToInt(); val fees = breakdownObject.optDouble("mandatoryFees", 0.0).roundToInt(); val property = breakdownObject.optDouble("payAtProperty", 0.0).roundToInt()
            if (room > 0) add("سعر الإقامة الأساسي" to room); if (taxes > 0) add("ضرائب" to taxes); if (fees > 0) add("ضرائب/رسوم إلزامية ضمن الإجمالي" to fees); if (property > 0) add("من الإجمالي يُدفع في الفندق" to property)
        }
        val strategy = raw.optString("strategyKind", "STANDARD"); val title = when (strategy) { "CARD" -> "اقتـناص ببطاقتك"; "MEMBER" -> "سعر أعضاء"; "APP" -> "سعر التطبيق"; "PROMO" -> "عرض موثق"; "PACKAGE" -> "تركيبة توفير"; else -> "سعر مباشر" }
        val verification = raw.optString("verification"); val source = raw.optString("provider", "مصدر الحجز")
        val steps = buildList { add("افتح مصدر الحجز: $source."); addAll(requirements); add("تأكد أن نفس الفندق والتواريخ والغرفة والسياسات ما زالت مطابقة قبل الدفع."); add("تحقق من أن الإجمالي النهائي الظاهر لا يتجاوز ${formatMoney(totalExact)} ${breakdownObject.optString("currency", "SAR")}.") }
        return DealOffer(
            id = raw.optString("id"), source = source, finalPrice = totalExact.roundToInt(), referencePrice = comparisonExact.roundToInt(), currency = breakdownObject.optString("currency", "SAR"),
            title = title, method = raw.optString("method", source), trust = if (verification == "LIVE_VERIFIED") PriceTrust.VERIFIED_LIVE else PriceTrust.DISCOVERED,
            matchPercent = raw.optInt("matchPercent", 100), lastChecked = raw.optString("verifiedAt", "تحقق حديث"), cancellation = raw.optString("cancellation", "راجع سياسة الإلغاء"), meal = raw.optString("meal", "حسب العرض"),
            conditions = requirements, steps = steps, breakdown = breakdown, cardRequirement = requirements.firstOrNull { it.contains("بطاقة") }, memberRequirement = requirements.firstOrNull { it.contains("عضوية") || it.contains("حساب") },
            bookingUrl = raw.optString("bookingUrl").takeIf { it.isNotBlank() }, evidenceUrl = raw.optString("evidenceUrl").takeIf { it.isNotBlank() }, providerHotelId = raw.optString("providerHotelId"), roomName = raw.optString("roomName").takeIf { it.isNotBlank() },
            finalPriceExact = totalExact, referencePriceExact = comparisonExact
        )
    }
}
private inline fun JSONArray.forEachObject(block: (JSONObject) -> Unit) { for (i in 0 until length()) optJSONObject(i)?.let(block) }
private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else buildList { for (i in 0 until length()) optString(i).takeIf { it.isNotBlank() }?.let(::add) }
private fun JSONObject.optNullableDouble(name: String): Double? = if (has(name) && !isNull(name)) optDouble(name) else null

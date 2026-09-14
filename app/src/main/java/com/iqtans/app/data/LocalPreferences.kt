package com.iqtans.app.data

import android.content.Context
import com.iqtans.app.domain.PriceWatch
import com.iqtans.app.domain.UserPaymentCard
import org.json.JSONArray
import org.json.JSONObject

class LocalPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("iqtans_local", Context.MODE_PRIVATE)

    fun loadCards(): List<UserPaymentCard> = parseArray(prefs.getString("cards", null)) { o ->
        val bank = o.optString("bank")
        if (bank.isBlank()) null else UserPaymentCard(
            id = o.optString("id", System.nanoTime().toString()),
            bank = bank,
            network = o.optString("network", "غير محددة"),
            tier = o.optString("tier", "غير محددة"),
            isEnabled = o.optBoolean("enabled", true)
        )
    }

    fun saveCards(cards: List<UserPaymentCard>) {
        val array = JSONArray()
        cards.forEach { card ->
            array.put(JSONObject().apply {
                put("id", card.id)
                put("bank", card.bank)
                put("network", card.network)
                put("tier", card.tier)
                put("enabled", card.isEnabled)
            })
        }
        prefs.edit().putString("cards", array.toString()).apply()
    }

    fun loadWatches(): List<PriceWatch> = parseArray(prefs.getString("watches", null)) { o ->
        val hotelId = o.optString("hotelId")
        if (hotelId.isBlank()) return@parseArray null
        val legacyPrice = o.optDouble("lastSeenPrice", 0.0)
        val savedPrice = o.optDouble("savedPrice", legacyPrice)
        val bestSeen = o.optDouble("bestSeenPrice", if (legacyPrice > 0) legacyPrice else savedPrice)
        PriceWatch(
            id = o.optString("id"),
            hotelId = hotelId,
            hotelName = o.optString("hotelName"),
            city = o.optString("city"),
            checkIn = o.optString("checkIn"),
            checkOut = o.optString("checkOut"),
            guests = o.optInt("guests", 2),
            rooms = o.optInt("rooms", 1),
            savedPrice = savedPrice,
            lastSeenPrice = legacyPrice.takeIf { it > 0 } ?: savedPrice,
            bestSeenPrice = bestSeen.takeIf { it > 0 } ?: savedPrice,
            targetPrice = optionalDouble(o, "targetPrice"),
            lastNotifiedPrice = optionalDouble(o, "lastNotifiedPrice"),
            currency = o.optString("currency", "SAR"),
            savedAt = o.optLong("savedAt", 0L),
            lastCheckedAt = o.optLong("lastCheckedAt", 0L)
        )
    }

    fun saveWatches(watches: List<PriceWatch>) {
        val array = JSONArray()
        watches.forEach { w ->
            array.put(JSONObject().apply {
                put("id", w.id)
                put("hotelId", w.hotelId)
                put("hotelName", w.hotelName)
                put("city", w.city)
                put("checkIn", w.checkIn)
                put("checkOut", w.checkOut)
                put("guests", w.guests)
                put("rooms", w.rooms)
                put("savedPrice", w.savedPrice)
                put("lastSeenPrice", w.lastSeenPrice)
                put("bestSeenPrice", w.bestSeenPrice)
                if (w.targetPrice != null) put("targetPrice", w.targetPrice) else put("targetPrice", JSONObject.NULL)
                if (w.lastNotifiedPrice != null) put("lastNotifiedPrice", w.lastNotifiedPrice) else put("lastNotifiedPrice", JSONObject.NULL)
                put("currency", w.currency)
                put("savedAt", w.savedAt)
                put("lastCheckedAt", w.lastCheckedAt)
            })
        }
        prefs.edit().putString("watches", array.toString()).apply()
    }

    private fun optionalDouble(o: JSONObject, key: String): Double? {
        if (!o.has(key) || o.isNull(key)) return null
        return o.optDouble(key, Double.NaN).takeIf { it.isFinite() && it > 0.0 }
    }

    private fun <T> parseArray(raw: String?, parser: (JSONObject) -> T?): List<T> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) array.optJSONObject(i)?.let(parser)?.let(::add)
            }
        }.getOrDefault(emptyList())
    }
}

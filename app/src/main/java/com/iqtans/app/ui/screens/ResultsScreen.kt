package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.DealOffer
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.PriceTrust
import com.iqtans.app.domain.SearchCoverage
import com.iqtans.app.domain.SearchRequest
import com.iqtans.app.ui.components.DemoModeBanner
import com.iqtans.app.ui.components.HotelDealCard
import com.iqtans.app.ui.theme.*

@Composable
fun ResultsScreen(
    request: SearchRequest,
    hotels: List<HotelDeal>,
    isLive: Boolean,
    coverage: SearchCoverage,
    onBack: () -> Unit,
    onHotelClick: (HotelDeal) -> Unit
) {
    var sort by remember { mutableStateOf("التوفير") }
    var showFilters by remember { mutableStateOf(false) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var perfectMatchOnly by remember { mutableStateOf(false) }
    var cancellableOnly by remember { mutableStateOf(false) }
    var breakfastOnly by remember { mutableStateOf(false) }
    var propertyPaymentOnly by remember { mutableStateOf(false) }

    val activeFilterCount = listOf(verifiedOnly, perfectMatchOnly, cancellableOnly, breakfastOnly, propertyPaymentOnly).count { it }
    val hasActiveFilters = activeFilterCount > 0

    fun offerMatches(offer: DealOffer): Boolean {
        if (verifiedOnly && offer.trust != PriceTrust.VERIFIED_LIVE) return false
        if (perfectMatchOnly && offer.matchPercent != 100) return false
        if (cancellableOnly && !isCancellable(offer)) return false
        if (breakfastOnly && !hasBreakfast(offer)) return false
        if (propertyPaymentOnly && !offer.paymentLabel.contains("الفندق")) return false
        return true
    }

    val filteredRows = remember(hotels, sort, verifiedOnly, perfectMatchOnly, cancellableOnly, breakfastOnly, propertyPaymentOnly) {
        hotels.mapNotNull { hotel ->
            val matchingOffers = hotel.offers.filter(::offerMatches)
            if (matchingOffers.isEmpty()) null
            else {
                val bestMatching = matchingOffers.minBy { it.finalAmount }
                Triple(hotel, bestMatching, if (hasActiveFilters) hotel.copy(offers = matchingOffers) else hotel)
            }
        }.let { rows ->
            when (sort) {
                "السعر" -> rows.sortedBy { it.second.finalAmount }
                "التقييم" -> rows.sortedByDescending { it.first.rating }
                else -> rows.sortedByDescending { it.second.savingsAmount }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("فلترة شروط الحجز", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("السعر الظاهر بعد الفلترة سيكون لأرخص عرض يحقق الشروط المختارة فعلًا.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                FilterSwitch("مؤكد حيًا فقط", "استبعد الأسعار المكتشفة أو التجريبية.", verifiedOnly) { verifiedOnly = it }
                FilterSwitch("تطابق 100%", "نفس الغرفة والشروط التي استطاع اقتنص إثباتها بالكامل.", perfectMatchOnly) { perfectMatchOnly = it }
                FilterSwitch("قابل للإلغاء", "اعرض العروض التي يذكر مصدرها أنها قابلة للإلغاء أو الاسترداد.", cancellableOnly) { cancellableOnly = it }
                FilterSwitch("إفطار مشمول", "اعرض العروض التي يذكر مصدرها الإفطار ضمن الوجبة.", breakfastOnly) { breakfastOnly = it }
                FilterSwitch("دفع في الفندق", "يشمل الدفع الكامل أو الجزئي في مكان الإقامة.", propertyPaymentOnly) { propertyPaymentOnly = it }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            verifiedOnly = false
                            perfectMatchOnly = false
                            cancellableOnly = false
                            breakfastOnly = false
                            propertyPaymentOnly = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = hasActiveFilters
                    ) { Text("مسح الفلاتر") }
                    Button(onClick = { showFilters = false }, modifier = Modifier.weight(1f)) { Text("عرض النتائج") }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 42.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowForwardIos, "رجوع") }
                Column(Modifier.weight(1f)) {
                    Text("نتائج الاقتناص", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("${request.city} • ${request.checkIn} — ${request.checkOut}", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Text(occupancyLabel(request), color = Muted, style = MaterialTheme.typography.labelMedium)
                }
                BadgedBox(badge = {
                    if (activeFilterCount > 0) Badge { Text(activeFilterCount.toString()) }
                }) {
                    FilledTonalIconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Rounded.FilterList, "فلترة النتائج", tint = Emerald)
                    }
                }
            }
        }

        if (!isLive) item { DemoModeBanner() }

        if (coverage.priceProvidersTotal > 0) {
            item { CoverageCard(coverage = coverage, isLive = isLive) }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("التوفير", "السعر", "التقييم").forEach { option ->
                    FilterChip(selected = sort == option, onClick = { sort = option }, label = { Text(option) })
                }
            }
        }

        if (hasActiveFilters) {
            item {
                Surface(color = SoftMint, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Tune, null, tint = Emerald)
                        Spacer(Modifier.width(8.dp))
                        Text("$activeFilterCount شروط فعالة • السعر في البطاقة مطابق لها", color = Emerald, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showFilters = true }) { Text("تعديل") }
                    }
                }
            }
        }

        if (filteredRows.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.SearchOff, null, tint = Emerald, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(if (hasActiveFilters) "لا يوجد عرض يحقق كل الشروط" else "لا توجد نتائج مطابقة بعد", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (hasActiveFilters) "خفف أحد الفلاتر أو افتح الفلاتر لتعديل شروط الحجز."
                            else "لم نجد سعرًا حيًا يطابق التواريخ والإشغال والشروط المطلوبة حاليًا.",
                            color = Muted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            item { Text("${filteredRows.size} فنادق • مرتبة حسب $sort", color = Muted, style = MaterialTheme.typography.bodyMedium) }
            items(filteredRows, key = { it.first.id }) { (hotel, bestMatching, filteredHotel) ->
                HotelDealCard(
                    hotel = hotel,
                    bestOfferOverride = if (hasActiveFilters) bestMatching else null,
                    onClick = { onHotelClick(filteredHotel) }
                )
            }
        }
    }
}

@Composable
private fun FilterSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

private fun isCancellable(offer: DealOffer): Boolean {
    val value = offer.cancellation.lowercase()
    return value.contains("قابل") || value.contains("مجاني") || value.contains("free") || value.contains("refundable")
}

private fun hasBreakfast(offer: DealOffer): Boolean {
    val value = offer.meal.lowercase()
    return value.contains("إفطار") || value.contains("افطار") || value.contains("breakfast")
}

@Composable
private fun CoverageCard(coverage: SearchCoverage, isLive: Boolean) {
    Surface(color = if (isLive) SoftMint else SoftSand, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Radar, null, tint = Emerald)
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("تغطية هذا البحث", fontWeight = FontWeight.Black, color = Ink)
                    Text("نريك ما تم فحصه فعلًا، بدون ادعاء تغطية مصادر غير متاحة.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
            Text(
                "مصادر الأسعار المفعلة: ${coverage.priceProvidersConfigured}/${coverage.priceProvidersTotal} • أعادت نتائج: ${coverage.priceProvidersWithResults}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            if (coverage.configuredProviderNames.isNotEmpty()) {
                Text("تم الفحص: ${coverage.configuredProviderNames.joinToString(" • ")}", color = Emerald, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Text("عروض أسعار حية تم استلامها: ${coverage.liveOffers}", color = Muted, style = MaterialTheme.typography.bodyMedium)
            if (coverage.discoveredLeads > 0) {
                Text("إشارات عروض من الويب: ${coverage.discoveredLeads} — لا تدخل في التوفير حتى التحقق.", color = Night, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            if (coverage.providerWarnings.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
                Text("مصادر لم تُكمل الفحص:", color = Night, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                coverage.providerWarnings.take(3).forEach { warning -> Text("• $warning", color = Muted, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

private fun occupancyLabel(request: SearchRequest): String {
    val children = if (request.childrenAges.isEmpty()) "بدون أطفال" else "${request.childrenAges.size} أطفال (${request.childrenAges.joinToString("، ")} سنة)"
    return "${request.guests} بالغ • $children • ${request.rooms} غرفة"
}

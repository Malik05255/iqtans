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
import com.iqtans.app.domain.HotelDeal
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
    val sorted = remember(hotels, sort) {
        when (sort) {
            "السعر" -> hotels.sortedBy { it.bestOffer.finalAmount }
            "التقييم" -> hotels.sortedByDescending { it.rating }
            else -> hotels.sortedByDescending { it.bestOffer.savingsAmount }
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
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Rounded.FilterList, null, tint = Emerald, modifier = Modifier.padding(11.dp).size(21.dp))
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

        if (sorted.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Search, null, tint = Emerald, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("لا توجد نتائج مطابقة بعد", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("لم نجد سعرًا حيًا يطابق التواريخ والإشغال والشروط المطلوبة حاليًا.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            item { Text("${sorted.size} فنادق • مرتبة حسب $sort", color = Muted, style = MaterialTheme.typography.bodyMedium) }
            items(sorted, key = { it.id }) { hotel -> HotelDealCard(hotel = hotel, onClick = { onHotelClick(hotel) }) }
        }
    }
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
            Text("عروض أسعار حية تم استلامها: ${coverage.liveOffers}", color = Muted, style = MaterialTheme.typography.bodyMedium)
            if (coverage.discoveredLeads > 0) {
                Text("إشارات عروض من الويب: ${coverage.discoveredLeads} — لا تدخل في التوفير حتى التحقق.", color = Night, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun occupancyLabel(request: SearchRequest): String {
    val children = if (request.childrenAges.isEmpty()) "بدون أطفال" else "${request.childrenAges.size} أطفال (${request.childrenAges.joinToString("، ")} سنة)"
    return "${request.guests} بالغ • $children • ${request.rooms} غرفة"
}

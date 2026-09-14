package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.SearchRequest
import com.iqtans.app.ui.components.DemoModeBanner
import com.iqtans.app.ui.components.HotelDealCard
import com.iqtans.app.ui.theme.Emerald
import com.iqtans.app.ui.theme.Muted

@Composable
fun ResultsScreen(request: SearchRequest, hotels: List<HotelDeal>, isLive: Boolean, onBack: () -> Unit, onHotelClick: (HotelDeal) -> Unit) {
    var sort by remember { mutableStateOf("التوفير") }
    val sorted = remember(hotels, sort) {
        when (sort) {
            "السعر" -> hotels.sortedBy { it.bestOffer.finalAmount }
            "التقييم" -> hotels.sortedByDescending { it.rating }
            else -> hotels.sortedByDescending { it.bestOffer.savingsAmount }
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 42.dp, bottom = 118.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowForwardIos, "رجوع") }
                Column(Modifier.weight(1f)) { Text("نتائج الاقتناص", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Text("${request.city} • ${request.checkIn} — ${request.checkOut}", color = Muted, style = MaterialTheme.typography.bodyMedium) }
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.FilterList, null, tint = Emerald, modifier = Modifier.padding(11.dp).size(21.dp)) }
            }
        }
        if (!isLive) item { DemoModeBanner() }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("التوفير", "السعر", "التقييم").forEach { option -> FilterChip(selected = sort == option, onClick = { sort = option }, label = { Text(option) }) } } }
        if (sorted.isEmpty()) item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.Search, null, tint = Emerald, modifier = Modifier.size(36.dp)); Spacer(Modifier.height(12.dp)); Text("لا توجد نتائج بعد", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text("لا توجد أسعار حية مطابقة لهذا البحث حاليًا.", color = Muted, style = MaterialTheme.typography.bodyMedium) } }
        } else {
            item { Text("${sorted.size} نتائج • مرتبة حسب $sort", color = Muted, style = MaterialTheme.typography.bodyMedium) }
            items(sorted, key = { it.id }) { hotel -> HotelDealCard(hotel = hotel, onClick = { onHotelClick(hotel) }) }
        }
    }
}

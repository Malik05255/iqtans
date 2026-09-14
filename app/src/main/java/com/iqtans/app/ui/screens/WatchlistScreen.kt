package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.ui.components.HotelDealCard
import com.iqtans.app.ui.theme.Emerald
import com.iqtans.app.ui.theme.Muted

@Composable
fun WatchlistScreen(
    hotels: List<HotelDeal>,
    onHotelClick: (HotelDeal) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("مراقبة الأسعار", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text("احفظ الفندق والتاريخ والغرفة، وليس اسم الفندق فقط.", color = Muted)
        }

        if (hotels.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.NotificationsNone, null, tint = Emerald, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("لا تراقب أي فندق بعد", fontWeight = FontWeight.Black)
                        Text("من صفحة الفندق اضغط جرس المراقبة.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = Emerald)
                        Spacer(Modifier.width(9.dp))
                        Text("${hotels.size} فنادق تحت المراقبة", fontWeight = FontWeight.Bold)
                    }
                }
            }
            items(hotels, key = { it.id }) { hotel ->
                HotelDealCard(hotel, onClick = { onHotelClick(hotel) })
            }
        }
    }
}

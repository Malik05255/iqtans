package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.PriceWatch
import com.iqtans.app.domain.formatMoney
import com.iqtans.app.ui.theme.*

@Composable
fun WatchlistScreen(watches: List<PriceWatch>, onCheck: (PriceWatch) -> Unit, onDelete: (PriceWatch) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 112.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("مراقبة الأسعار", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Spacer(Modifier.height(5.dp)); Text("نحفظ الفندق مع نفس التواريخ والنزلاء. السعر المخزن مجرد آخر سعر شوهد؛ اضغط إعادة فحص للحصول على سعر حي جديد.", color = Muted, style = MaterialTheme.typography.bodyMedium) }
        if (watches.isEmpty()) item { Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.NotificationsNone, null, tint = Emerald, modifier = Modifier.size(40.dp)); Spacer(Modifier.height(12.dp)); Text("لا تراقب أي إقامة بعد", fontWeight = FontWeight.Black); Text("من صفحة الفندق اضغط جرس المراقبة.", color = Muted) } } }
        else {
            item { Surface(color = SoftMint, shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.NotificationsActive, null, tint = Emerald); Spacer(Modifier.width(9.dp)); Text("${watches.size} إقامات محفوظة للمراقبة", fontWeight = FontWeight.Bold) } } }
            items(watches, key = { it.id }) { watch ->
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) { Column(Modifier.weight(1f)) { Text(watch.hotelName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium); Text("${watch.city} • ${watch.checkIn} — ${watch.checkOut}", color = Muted, style = MaterialTheme.typography.bodyMedium); Text("${watch.guests} نزلاء • ${watch.rooms} غرفة", color = Muted, style = MaterialTheme.typography.labelMedium) }; IconButton(onClick = { onDelete(watch) }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = Muted) } }
                    Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("آخر سعر شوهد", color = Muted, style = MaterialTheme.typography.labelMedium); Text("${formatMoney(watch.lastSeenPrice)} ${watch.currency}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) }; Button(onClick = { onCheck(watch) }, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("إعادة فحص") } }
                } }
            }
        }
    }
}

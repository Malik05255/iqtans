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
import com.iqtans.app.domain.PriceWatch
import com.iqtans.app.domain.formatMoney
import com.iqtans.app.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WatchlistScreen(
    watches: List<PriceWatch>,
    onCheck: (PriceWatch) -> Unit,
    onDelete: (PriceWatch) -> Unit,
    onTargetChange: (PriceWatch, Double?) -> Unit
) {
    var editingTargetFor by remember { mutableStateOf<PriceWatch?>(null) }
    var targetText by remember { mutableStateOf("") }

    editingTargetFor?.let { watch ->
        AlertDialog(
            onDismissRequest = { editingTargetFor = null },
            title = { Text("سعر التنبيه", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("نبّهني عندما يصل أفضل سعر حي لنفس الإقامة إلى هذا المبلغ أو أقل.", color = Muted)
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("السعر بـ ${watch.currency}") },
                        singleLine = true
                    )
                    if (watch.targetPrice != null) {
                        TextButton(onClick = { onTargetChange(watch, null); editingTargetFor = null }) {
                            Text("إلغاء سعر التنبيه")
                        }
                    }
                }
            },
            confirmButton = {
                val target = targetText.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
                Button(onClick = { onTargetChange(watch, target); editingTargetFor = null }, enabled = target != null) {
                    Text("حفظ")
                }
            },
            dismissButton = { TextButton(onClick = { editingTargetFor = null }) { Text("رجوع") } }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("مراقبة الأسعار", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text("اقتنص يعيد الفحص دوريًا بنفس التواريخ والبالغين وأعمار الأطفال والغرف. السعر المخزن لا يُعتبر حيًا حتى آخر تحقق.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }

        if (watches.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.NotificationsNone, null, tint = Emerald, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("لا تراقب أي إقامة بعد", fontWeight = FontWeight.Black)
                        Text("من صفحة الفندق اضغط جرس المراقبة.", color = Muted)
                    }
                }
            }
        } else {
            item {
                Surface(color = SoftMint, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = Emerald)
                        Spacer(Modifier.width(9.dp))
                        Text("${watches.size} إقامات محفوظة للمراقبة", fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(watches, key = { it.id }) { watch ->
                val savedAmount = (watch.savedPrice - watch.bestSeenPrice).coerceAtLeast(0.0)
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(watch.hotelName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                Text("${watch.city} • ${watch.checkIn} — ${watch.checkOut}", color = Muted, style = MaterialTheme.typography.bodyMedium)
                                Text(occupancyLabel(watch), color = Muted, style = MaterialTheme.typography.labelMedium)
                            }
                            IconButton(onClick = { onDelete(watch) }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = Muted) }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PriceMetric("عند الحفظ", watch.savedPrice, watch.currency, Modifier.weight(1f))
                            PriceMetric("آخر سعر", watch.lastSeenPrice, watch.currency, Modifier.weight(1f))
                            PriceMetric("الأفضل", watch.bestSeenPrice, watch.currency, Modifier.weight(1f), highlight = true)
                        }

                        if (savedAmount > 0.009) {
                            Spacer(Modifier.height(10.dp))
                            Surface(color = SoftMint, shape = RoundedCornerShape(13.dp)) {
                                Text("أقل سعر وجدناه وفر ${formatMoney(savedAmount)} ${watch.currency} عن وقت الحفظ", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Emerald, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    watch.targetPrice?.let { "تنبيه عند ${formatMoney(it)} ${watch.currency} أو أقل" } ?: "لا يوجد سعر تنبيه محدد",
                                    color = if (watch.targetPrice != null) Emerald else Muted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (watch.targetPrice != null) FontWeight.Bold else FontWeight.Normal
                                )
                                if (watch.lastCheckedAt > 0L) {
                                    Text("آخر فحص ${formatTime(watch.lastCheckedAt)}", color = Muted, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            TextButton(onClick = {
                                targetText = watch.targetPrice?.let(::formatMoney).orEmpty()
                                editingTargetFor = watch
                            }) { Text("سعر التنبيه") }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(onClick = { onCheck(watch) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("إعادة فحص الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun occupancyLabel(watch: PriceWatch): String {
    val children = if (watch.childrenAges.isEmpty()) {
        "بدون أطفال"
    } else {
        "${watch.childrenAges.size} أطفال (${watch.childrenAges.joinToString("، ")} سنة)"
    }
    return "${watch.guests} بالغ • $children • ${watch.rooms} غرفة"
}

@Composable
private fun PriceMetric(label: String, price: Double, currency: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Surface(modifier = modifier, color = if (highlight) SoftMint else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
            Text("${formatMoney(price)} $currency", fontWeight = FontWeight.Black, color = if (highlight) Emerald else Ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatTime(epochMillis: Long): String = runCatching {
    val formatter = DateTimeFormatter.ofPattern("dd/MM • HH:mm")
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
}.getOrDefault("حديثًا")

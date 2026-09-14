package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqtans.app.domain.DealOffer
import com.iqtans.app.domain.PriceTrust
import com.iqtans.app.ui.components.TrustChip
import com.iqtans.app.ui.theme.*

@Composable
fun DealDetailScreen(
    hotelName: String,
    offer: DealOffer,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 42.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowForwardIos, "رجوع") }
                Column(Modifier.weight(1f)) {
                    Text("كيف تحصل على السعر؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(hotelName, color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Surface(color = Night, shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustChip(offer.trust)
                        Spacer(Modifier.weight(1f))
                        Text(offer.source, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .7f), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("${offer.finalPrice} ${offer.currency}", color = androidx.compose.ui.graphics.Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                    if (offer.savings > 0) {
                        Text("وفرت ${offer.savings} ر.س عن أفضل سعر مرجعي مطابق", color = Sand, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(offer.method, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .86f))
                }
            }
        }

        if (offer.trust == PriceTrust.DEMO) {
            item {
                Surface(color = SoftSand, shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.WarningAmber, null, tint = Night)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "هذا السيناريو توضيحي للواجهة وليس سعرًا من السوق. زر الحجز الحقيقي سيُفعّل فقط بعد إعادة التحقق من مزود حي.",
                            color = Night,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (offer.breakdown.isNotEmpty()) {
            item {
                Text("كيف تكوّن السعر؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(9.dp))
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        offer.breakdown.forEachIndexed { index, (label, amount) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                                Text(label, modifier = Modifier.weight(1f), color = if (amount < 0) Emerald else Ink)
                                Text(
                                    if (amount < 0) "${amount} ر.س" else "$amount ر.س",
                                    fontWeight = FontWeight.Black,
                                    color = if (amount < 0) Emerald else Ink
                                )
                            }
                            if (index != offer.breakdown.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("الإجمالي النهائي", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black)
                            Text("${offer.finalPrice} ر.س", fontWeight = FontWeight.Black, color = Emerald, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("الخطوات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(9.dp))
            val steps = offer.steps.ifEmpty { listOf("افتح مصدر الحجز.", "اختر نفس الفندق والغرفة والتواريخ.", "راجع الإجمالي النهائي قبل الدفع.") }
            steps.forEachIndexed { index, step ->
                StepRow(index + 1, step)
                if (index != steps.lastIndex) Spacer(Modifier.height(9.dp))
            }
        }

        if (offer.conditions.isNotEmpty() || offer.cardRequirement != null || offer.memberRequirement != null) {
            item {
                Text("شروط مهمة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(9.dp))
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        offer.cardRequirement?.let { ConditionRow(Icons.Rounded.CreditCard, "البطاقة", it) }
                        offer.memberRequirement?.let { ConditionRow(Icons.Rounded.PersonAdd, "العضوية", it) }
                        offer.conditions.forEach { ConditionRow(Icons.Rounded.CheckCircle, "شرط", it) }
                        ConditionRow(Icons.Rounded.SyncAlt, "الإلغاء", offer.cancellation)
                        ConditionRow(Icons.Rounded.Restaurant, "الوجبات", offer.meal)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {},
                enabled = offer.trust == PriceTrust.VERIFIED_LIVE,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text(if (offer.trust == PriceTrust.VERIFIED_LIVE) "اقتنص العرض" else "الحجز يتفعّل بعد التحقق الحي", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = SoftMint, shape = RoundedCornerShape(12.dp)) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    Text(number.toString(), color = Emerald, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(11.dp))
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ConditionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Emerald, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}

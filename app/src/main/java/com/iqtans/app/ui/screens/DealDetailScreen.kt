package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqtans.app.domain.*
import com.iqtans.app.ui.components.TrustChip
import com.iqtans.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun DealDetailScreen(hotelName: String, offer: DealOffer, onBack: () -> Unit, onVerify: (suspend () -> OfferVerification)? = null) {
    val uriHandler = LocalUriHandler.current; val scope = rememberCoroutineScope()
    var verifying by remember(offer.id) { mutableStateOf(false) }; var verification by remember(offer.id) { mutableStateOf<OfferVerification?>(null) }; var verifyError by remember(offer.id) { mutableStateOf<String?>(null) }
    val canVerify = offer.trust == PriceTrust.VERIFIED_LIVE && offer.matchPercent == 100 && offer.providerHotelId.isNotBlank() && onVerify != null
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 42.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowForwardIos, "رجوع") }; Column(Modifier.weight(1f)) { Text("كيف تحصل على السعر؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Text(hotelName, color = Muted, style = MaterialTheme.typography.bodyMedium) } } }
        item { Surface(color = Night, shape = RoundedCornerShape(28.dp)) { Column(Modifier.padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { TrustChip(offer.trust); Spacer(Modifier.weight(1f)); Text(offer.source, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .7f), style = MaterialTheme.typography.bodyMedium) }; Spacer(Modifier.height(16.dp)); Text("${formatMoney(offer.finalAmount)} ${offer.currency}", color = androidx.compose.ui.graphics.Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black); if (offer.savingsAmount > 0.0) Text("وفرت ${formatMoney(offer.savingsAmount)} ${offer.currency} عن أرخص سعر مرجعي مطابق", color = Sand, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text(offer.method, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .86f)) } } }
        if (offer.trust == PriceTrust.DEMO) item { Notice("هذا السيناريو توضيحي وليس سعرًا من السوق.") }
        if (offer.matchPercent < 100 && offer.trust == PriceTrust.VERIFIED_LIVE) item { Notice("السعر حي، لكن التطابق ${offer.matchPercent}% فقط. نعرضه للمقارنة ولا نفعّل الحجز حتى يصبح تطابق الغرفة والشروط 100%.") }
        if (offer.breakdown.isNotEmpty()) item {
            Text("كيف تكوّن السعر؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Spacer(Modifier.height(9.dp)); Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(16.dp)) { offer.breakdown.forEachIndexed { index, (label, amount) -> Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) { Text(label, modifier = Modifier.weight(1f), color = if (amount < 0) Emerald else Ink); Text("$amount ${offer.currency}", fontWeight = FontWeight.Black, color = if (amount < 0) Emerald else Ink) }; if (index != offer.breakdown.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .45f)) }; HorizontalDivider(Modifier.padding(vertical = 8.dp)); Row(Modifier.fillMaxWidth()) { Text("الإجمالي النهائي", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black); Text("${formatMoney(offer.finalAmount)} ${offer.currency}", fontWeight = FontWeight.Black, color = Emerald, fontSize = 18.sp) } } }
        }
        item { Text("الخطوات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Spacer(Modifier.height(9.dp)); val steps = offer.steps.ifEmpty { listOf("افتح مصدر الحجز.", "اختر نفس الفندق والغرفة والتواريخ.", "راجع الإجمالي النهائي قبل الدفع.") }; steps.forEachIndexed { index, step -> StepRow(index + 1, step); if (index != steps.lastIndex) Spacer(Modifier.height(9.dp)) } }
        if (offer.conditions.isNotEmpty() || offer.cardRequirement != null || offer.memberRequirement != null) item { Text("شروط مهمة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Spacer(Modifier.height(9.dp)); Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { offer.cardRequirement?.let { ConditionRow(Icons.Rounded.CreditCard, "البطاقة", it) }; offer.memberRequirement?.let { ConditionRow(Icons.Rounded.PersonAdd, "العضوية", it) }; offer.conditions.forEach { ConditionRow(Icons.Rounded.CheckCircle, "شرط", it) }; ConditionRow(Icons.Rounded.SyncAlt, "الإلغاء", offer.cancellation); ConditionRow(Icons.Rounded.Restaurant, "الوجبات", offer.meal) } } }
        offer.evidenceUrl?.let { evidence -> item { OutlinedButton(onClick = { runCatching { uriHandler.openUri(evidence) } }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp)) { Icon(Icons.Rounded.FactCheck, null); Spacer(Modifier.width(8.dp)); Text("فتح مصدر السعر أو شروط العرض", fontWeight = FontWeight.Bold) } } }
        verification?.let { result -> item {
            val current = result.currentPrice; val higher = current != null && current > result.expectedPrice + 0.004; val same = current != null && abs(current - result.expectedPrice) < 0.005
            Surface(color = if (!result.available || higher) SoftSand else SoftMint, shape = RoundedCornerShape(20.dp)) { Column(Modifier.fillMaxWidth().padding(15.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (result.available) Icons.Rounded.Verified else Icons.Rounded.ErrorOutline, null, tint = if (result.available) Emerald else Night); Spacer(Modifier.width(9.dp)); Text(if (!result.available) "العرض لم يعد متاحًا بنفس التطابق" else if (same) "أعيد التحقق: السعر ما زال ثابتًا" else if (current != null && current < result.expectedPrice) "السعر انخفض بعد إعادة التحقق" else "السعر ارتفع قبل الحجز", fontWeight = FontWeight.Black) }; if (current != null) Text("السابق ${formatMoney(result.expectedPrice)} ${offer.currency} • الحالي ${formatMoney(current)} ${offer.currency}", color = Muted, modifier = Modifier.padding(top = 5.dp)); result.reason?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp)) } } }
        } }
        verifyError?.let { item { Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(18.dp)) { Text(it, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer) } } }
        item {
            val verifiedHigher = verification?.let { it.available && it.currentPrice != null && it.currentPrice > it.expectedPrice + 0.004 && !it.bookingUrl.isNullOrBlank() } == true
            Button(onClick = {
                if (verifiedHigher) verification?.bookingUrl?.let { runCatching { uriHandler.openUri(it) } }
                else if (canVerify && !verifying) { verifying = true; verifyError = null; scope.launch { try { val result = onVerify!!.invoke(); verification = result; val current = result.currentPrice; if (result.available && current != null && current <= result.expectedPrice + 0.004 && !result.bookingUrl.isNullOrBlank()) runCatching { uriHandler.openUri(result.bookingUrl) } } catch (t: Throwable) { verifyError = t.message ?: "تعذر إعادة التحقق من السعر" } finally { verifying = false } } }
            }, enabled = canVerify && (!verifying || verifiedHigher), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) {
                if (verifying) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(if (verifiedHigher) Icons.Rounded.OpenInNew else Icons.Rounded.Refresh, null); Spacer(Modifier.width(8.dp)); Text(when { verifying -> "جاري إعادة التحقق…"; verifiedHigher -> "متابعة بالسعر الجديد"; canVerify -> "تحقق الآن واقتنص"; offer.matchPercent < 100 -> "الحجز يتطلب تطابق 100%"; else -> "الحجز يتفعّل بعد التحقق الحي" }, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable private fun Notice(text: String) { Surface(color = SoftSand, shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Rounded.WarningAmber, null, tint = Night); Spacer(Modifier.width(10.dp)); Text(text, color = Night, style = MaterialTheme.typography.bodyMedium) } } }
@Composable private fun StepRow(number: Int, text: String) { Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = SoftMint, shape = RoundedCornerShape(12.dp)) { Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) { Text(number.toString(), color = Emerald, fontWeight = FontWeight.Black) } }; Spacer(Modifier.width(11.dp)); Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge) } } }
@Composable private fun ConditionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) { Row(verticalAlignment = Alignment.Top) { Icon(icon, null, tint = Emerald, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(9.dp)); Column { Text(label, style = MaterialTheme.typography.labelMedium, color = Muted); Text(value, fontWeight = FontWeight.Medium) } } }

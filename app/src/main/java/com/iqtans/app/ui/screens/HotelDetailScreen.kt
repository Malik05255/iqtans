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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqtans.app.domain.*
import com.iqtans.app.ui.components.HotelArtwork
import com.iqtans.app.ui.components.SectionTitle
import com.iqtans.app.ui.components.TrustChip
import com.iqtans.app.ui.theme.*

@Composable
fun HotelDetailScreen(hotel: HotelDeal, watched: Boolean, onBack: () -> Unit, onToggleWatch: () -> Unit, onOfferClick: (DealOffer) -> Unit) {
    val best = hotel.bestOffer; val uriHandler = LocalUriHandler.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 42.dp, bottom = 42.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowForwardIos, "رجوع") }; Spacer(Modifier.weight(1f)); IconButton(onClick = onToggleWatch) { Icon(if (watched) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone, "مراقبة السعر", tint = if (watched) Emerald else MaterialTheme.colorScheme.onBackground) } }
            HotelArtwork(hotel.name, Modifier.fillMaxWidth().height(210.dp)); Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) { Text(hotel.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Spacer(Modifier.height(4.dp)); Text("${hotel.area} • ${hotel.distanceLabel}", color = Muted); Spacer(Modifier.height(7.dp)); Row { repeat(hotel.stars) { Icon(Icons.Rounded.Star, null, tint = Sand, modifier = Modifier.size(16.dp)) } } }
                Surface(color = SoftMint, shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(if (hotel.rating > 0) "${hotel.rating}" else "—", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Emerald); Text(if (hotel.ratingCount > 0) "${hotel.ratingCount} تقييم" else "التقييم غير متاح", style = MaterialTheme.typography.labelMedium, color = Emerald) } }
            }
        }
        item {
            Card(onClick = { onOfferClick(best) }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Night)) {
                Column(Modifier.padding(19.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Surface(color = Sand, shape = RoundedCornerShape(999.dp)) { Text("أفضل اقتناص", modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = Night, fontWeight = FontWeight.Black) }; Spacer(Modifier.weight(1f)); TrustChip(best.trust) }
                    Spacer(Modifier.height(16.dp)); Text("${formatMoney(best.finalAmount)} ${best.currency}", color = androidx.compose.ui.graphics.Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                    if (best.savingsAmount > 0.0) Text("بدل ${formatMoney(best.referenceAmount)} ${best.currency} • وفّر ${formatMoney(best.savingsAmount)} ${best.currency} (${best.savingsPercent}%)", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .76f))
                    Spacer(Modifier.height(13.dp)); Text(best.method, color = Sand, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Verified, null, tint = Mint, modifier = Modifier.size(18.dp)); Text("  تطابق ${best.matchPercent}% • ${best.cancellation} • ${best.meal}", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .86f), style = MaterialTheme.typography.bodyMedium) }; Spacer(Modifier.height(14.dp)); Text("اضغط لمعرفة كيف تحصل على السعر ←", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Surface(color = SoftSand, shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Rounded.Lightbulb, null, tint = Night); Spacer(Modifier.width(11.dp)); Column { Text("معلومة قد لا تعرفها", color = Night, fontWeight = FontWeight.Black); Text(hotel.insight, color = Night.copy(alpha = .76f), style = MaterialTheme.typography.bodyMedium) } } } }
        if (hotel.flexibilitySavingAmount > 0.0 && hotel.flexibleDateLabel != null) item { Surface(color = SoftMint, shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = androidx.compose.ui.graphics.Color.White, shape = RoundedCornerShape(13.dp)) { Icon(Icons.Rounded.EventRepeat, null, tint = Emerald, modifier = Modifier.padding(10.dp)) }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text("مرن؟ يمكنك توفير أكثر", fontWeight = FontWeight.Black, color = Ink); Text("${hotel.flexibleDateLabel} يوفر ${formatMoney(hotel.flexibilitySavingAmount)} ${best.currency} على نفس مدة الإقامة.", color = Muted, style = MaterialTheme.typography.bodyMedium) } } } }
        if (hotel.discoveries.isNotEmpty()) {
            item { SectionTitle("اكتشفنا لك — يحتاج تحقق", "إشارات من الويب والسوشال المفهرس؛ لا تدخل في رقم التوفير قبل إثبات شروطها.") }
            items(hotel.discoveries, key = { it.url }) { lead -> Card(onClick = { runCatching { uriHandler.openUri(lead.url) } }, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(15.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.TravelExplore, null, tint = Emerald); Spacer(Modifier.width(9.dp)); Text(lead.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Black); Icon(Icons.Rounded.OpenInNew, null, tint = Muted, modifier = Modifier.size(17.dp)) }; Spacer(Modifier.height(5.dp)); Text(lead.source, color = Emerald, style = MaterialTheme.typography.labelMedium); lead.description?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodyMedium, maxLines = 3) }; Spacer(Modifier.height(8.dp)); Surface(color = SoftSand, shape = RoundedCornerShape(999.dp)) { Text("غير محتسب في التوفير حتى التحقق", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Night, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) } } } }
        }
        item { SectionTitle("كل طرق الحجز", "الترتيب حسب التكلفة النهائية، لا نسبة الخصم الدعائية.") }
        items(hotel.offers.sortedBy { it.finalAmount }, key = { it.id }) { offer -> OfferRow(offer) { onOfferClick(offer) } }
        if (hotel.reviewSources.isNotEmpty()) item { SectionTitle("التقييم من مصادره", "المصدر وعدد المراجعات ظاهر بدل رقم مجهول."); Spacer(Modifier.height(10.dp)); hotel.reviewSources.forEach { review -> Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(review.source, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold); Icon(Icons.Rounded.Star, null, tint = Sand, modifier = Modifier.size(17.dp)); Text(" ${review.score}/${review.scale}", fontWeight = FontWeight.Black); Text(" • ${review.count}", color = Muted, style = MaterialTheme.typography.bodyMedium) } } } }
    }
}

@Composable
private fun OfferRow(offer: DealOffer, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(offer.source, fontWeight = FontWeight.Black); Text(offer.method, color = Muted, style = MaterialTheme.typography.bodyMedium) }; Column(horizontalAlignment = Alignment.End) { Text("${formatMoney(offer.finalAmount)} ${offer.currency}", fontWeight = FontWeight.Black, fontSize = 20.sp); if (offer.savingsAmount > 0.0) Text("وفّر ${formatMoney(offer.savingsAmount)}", color = Emerald, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) } }
            Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { TrustChip(offer.trust); Spacer(Modifier.width(7.dp)); Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(999.dp)) { Text("تطابق ${offer.matchPercent}%", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = Muted) } }
        }
    }
}

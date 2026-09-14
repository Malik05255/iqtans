package com.iqtans.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.PriceTrust
import com.iqtans.app.domain.formatMoney
import com.iqtans.app.ui.theme.*

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(if (compact) 38.dp else 48.dp).clip(RoundedCornerShape(if (compact) 12.dp else 15.dp)).background(Night), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.LocationOn, null, tint = Mint, modifier = Modifier.size(if (compact) 26.dp else 32.dp))
            Box(Modifier.align(Alignment.BottomEnd).padding(if (compact) 3.dp else 4.dp).size(if (compact) 14.dp else 17.dp).clip(CircleShape).background(Sand), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Night, modifier = Modifier.size(if (compact) 10.dp else 12.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text("اقتنص", style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DemoModeBanner(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = SoftSand, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Info, null, tint = Night); Spacer(Modifier.width(10.dp))
            Column { Text("وضع غير حي", fontWeight = FontWeight.Bold, color = Night); Text("الأسعار المعروضة توضيحية حتى يتصل التطبيق بمحرك اقتنص ومزودي الأسعار. السعر التجريبي لا يحمل شارة «مؤكد حيًا».", style = MaterialTheme.typography.bodyMedium, color = Night.copy(alpha = .78f)) }
        }
    }
}

@Composable
fun TrustChip(trust: PriceTrust, modifier: Modifier = Modifier) {
    val label: String; val bg: Color; val fg: Color; val icon: androidx.compose.ui.graphics.vector.ImageVector
    when (trust) {
        PriceTrust.VERIFIED_LIVE -> { label = "مؤكد حيًا"; bg = SoftMint; fg = Emerald; icon = Icons.Rounded.Verified }
        PriceTrust.DISCOVERED -> { label = "عرض مكتشف"; bg = SoftSand; fg = Night; icon = Icons.Rounded.Search }
        PriceTrust.DEMO -> { label = "توضيحي"; bg = MaterialTheme.colorScheme.surfaceVariant; fg = Muted; icon = Icons.Rounded.Science }
    }
    Surface(modifier, color = bg, shape = RoundedCornerShape(999.dp)) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(5.dp)); Text(label, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); subtitle?.let { Spacer(Modifier.height(3.dp)); Text(it, style = MaterialTheme.typography.bodyMedium, color = Muted) } }
}

@Composable
fun HotelArtwork(name: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Night, Emerald, Color(0xFF20866B))))) {
        Box(Modifier.align(Alignment.TopEnd).padding(18.dp).size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = .08f)))
        Box(Modifier.align(Alignment.BottomStart).padding(16.dp).size(width = 112.dp, height = 46.dp).clip(RoundedCornerShape(16.dp)).background(Sand.copy(alpha = .95f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Hotel, null, tint = Night, modifier = Modifier.size(28.dp)) }
        Text(name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.TopStart).padding(18.dp).widthIn(max = 180.dp))
    }
}

@Composable
fun HotelDealCard(hotel: HotelDeal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val best = hotel.bestOffer
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(14.dp)) {
            HotelArtwork(hotel.name, Modifier.fillMaxWidth().height(142.dp)); Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) { Text(hotel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black); Spacer(Modifier.height(4.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.LocationOn, null, tint = Muted, modifier = Modifier.size(15.dp)); Text(" ${hotel.area} • ${hotel.distanceLabel}", style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
                if (hotel.rating > 0) Surface(color = SoftMint, shape = RoundedCornerShape(12.dp)) { Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Star, null, tint = Emerald, modifier = Modifier.size(15.dp)); Text(" ${hotel.rating}", fontWeight = FontWeight.Black, color = Emerald) } }
            }
            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .6f)); Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) { Text("أفضل اقتناص", style = MaterialTheme.typography.labelMedium, color = Emerald, fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.Bottom) { Text(formatMoney(best.finalAmount), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink); Text(" ${best.currency}", style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(bottom = 4.dp)) }; Text("الإجمالي النهائي حسب بيانات المزود", style = MaterialTheme.typography.labelMedium, color = Muted) }
                Column(horizontalAlignment = Alignment.End) { if (best.savingsAmount > 0.0) Surface(color = SoftSand, shape = RoundedCornerShape(12.dp)) { Text("وفّر ${formatMoney(best.savingsAmount)} ${best.currency}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Night, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(6.dp)); TrustChip(best.trust) }
            }
        }
    }
}

@Composable
fun SearchFieldTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(modifier = modifier, onClick = { onClick?.invoke() }, enabled = onClick != null, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Emerald, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(10.dp)); Column { Text(label, style = MaterialTheme.typography.labelMedium, color = Muted); Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ink) } }
    }
}

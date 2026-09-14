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
import com.iqtans.app.ui.theme.*

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (compact) 38.dp else 48.dp)
                .clip(RoundedCornerShape(if (compact) 12.dp else 15.dp))
                .background(Night),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Mint,
                modifier = Modifier.size(if (compact) 26.dp else 32.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(if (compact) 3.dp else 4.dp)
                    .size(if (compact) 14.dp else 17.dp)
                    .clip(CircleShape)
                    .background(Sand),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Night,
                    modifier = Modifier.size(if (compact) 10.dp else 12.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "اقتنص",
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun DemoModeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SoftSand,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null, tint = Night)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("نسخة تصميم وتجربة", fontWeight = FontWeight.Bold, color = Night)
                Text(
                    "الأسعار الحالية توضيحية حتى ربط مزودي الأسعار الحية. لن نضع علامة «مؤكد» على سعر تجريبي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Night.copy(alpha = .78f)
                )
            }
        }
    }
}

@Composable
fun TrustChip(trust: PriceTrust, modifier: Modifier = Modifier) {
    val (label, bg, fg, icon) = when (trust) {
        PriceTrust.VERIFIED_LIVE -> Quad("مؤكد حيًا", SoftMint, Emerald, Icons.Rounded.Verified)
        PriceTrust.DISCOVERED -> Quad("عرض مكتشف", SoftSand, Night, Icons.Rounded.Search)
        PriceTrust.DEMO -> Quad("توضيحي", MaterialTheme.colorScheme.surfaceVariant, Muted, Icons.Rounded.Science)
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(999.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold)
        }
    }
}

private data class Quad(
    val label: String,
    val bg: Color,
    val fg: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        if (subtitle != null) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
fun HotelArtwork(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Night, Emerald, Color(0xFF20866B))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .08f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(width = 112.dp, height = 46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Sand.copy(alpha = .95f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Hotel, contentDescription = null, tint = Night, modifier = Modifier.size(28.dp))
        }
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
                .widthIn(max = 180.dp)
        )
    }
}

@Composable
fun HotelDealCard(
    hotel: HotelDeal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val best = hotel.bestOffer
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            HotelArtwork(hotel.name, Modifier.fillMaxWidth().height(142.dp))
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(hotel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Muted, modifier = Modifier.size(15.dp))
                        Text(" ${hotel.area} • ${hotel.distanceLabel}", style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Surface(color = SoftMint, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Emerald, modifier = Modifier.size(15.dp))
                        Text(" ${hotel.rating}", fontWeight = FontWeight.Black, color = Emerald)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .6f))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("أفضل اقتناص", style = MaterialTheme.typography.labelMedium, color = Emerald, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${best.finalPrice}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
                        Text(" ${best.currency}", style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("شامل حسب شروط العرض", style = MaterialTheme.typography.labelMedium, color = Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(color = SoftSand, shape = RoundedCornerShape(12.dp)) {
                        Text("وفّر ${best.savings} ر.س", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Night, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(6.dp))
                    TrustChip(best.trust)
                }
            }
        }
    }
}

@Composable
fun SearchFieldTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)
    ) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Emerald, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
    }
}

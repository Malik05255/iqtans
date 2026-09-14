package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.UserPaymentCard
import com.iqtans.app.ui.theme.*

@Composable
fun CardsScreen() {
    var cards by remember {
        mutableStateOf(
            listOf(
                UserPaymentCard("1", "الراجحي", "Visa", "Signature"),
                UserPaymentCard("2", "SNB", "Mastercard", "World")
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 112.dp)
    ) {
        Text("بطاقاتي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(5.dp))
        Text("نحتاج اسم البنك وشبكة البطاقة وفئتها فقط. لا نحفظ رقم البطاقة أو CVV.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))

        Surface(color = SoftMint, shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Security, null, tint = Emerald)
                Spacer(Modifier.width(10.dp))
                Text("نستخدم هذه المعلومات فقط لمعرفة العروض التي تنطبق عليك.", color = Ink, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(16.dp))
        cards.forEach { card ->
            PaymentCardTile(card)
            Spacer(Modifier.height(11.dp))
        }

        OutlinedButton(
            onClick = {
                if (cards.none { it.id == "3" }) {
                    cards = cards + UserPaymentCard("3", "بنك الرياض", "Visa", "Platinum")
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(17.dp)
        ) {
            Icon(Icons.Rounded.Add, null)
            Spacer(Modifier.width(7.dp))
            Text("إضافة نوع بطاقة", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentCardTile(card: UserPaymentCard) {
    Surface(
        color = Night,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Sand, shape = RoundedCornerShape(13.dp)) {
                Icon(Icons.Rounded.CreditCard, null, tint = Night, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(card.bank, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                Text("${card.network} • ${card.tier}", color = Color.White.copy(alpha = .7f))
            }
            Surface(color = Mint.copy(alpha = .15f), shape = RoundedCornerShape(999.dp)) {
                Text("مفعّلة", color = Mint, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

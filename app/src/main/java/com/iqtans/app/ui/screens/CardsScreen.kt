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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.domain.UserPaymentCard
import com.iqtans.app.ui.theme.*

@Composable
fun CardsScreen(cards: List<UserPaymentCard>, onCardsChange: (List<UserPaymentCard>) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var bank by remember { mutableStateOf("") }
    var network by remember { mutableStateOf("Visa") }
    var tier by remember { mutableStateOf("") }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("إضافة نوع بطاقة", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("لا تدخل رقم البطاقة أو تاريخ الانتهاء أو CVV.", color = Muted)
                    OutlinedTextField(bank, { bank = it }, label = { Text("البنك") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("الشبكة", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Visa", "Mastercard", "mada").forEach { item ->
                            FilterChip(selected = network == item, onClick = { network = item }, label = { Text(item) })
                        }
                    }
                    OutlinedTextField(tier, { tier = it }, label = { Text("الفئة — مثال Signature / World / Platinum") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    onCardsChange(cards + UserPaymentCard(System.currentTimeMillis().toString(), bank.trim(), network, tier.trim().ifBlank { "غير محددة" }))
                    bank = ""; tier = ""; network = "Visa"; showAdd = false
                }, enabled = bank.isNotBlank()) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("إلغاء") } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Text("بطاقاتي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text("نحتاج اسم البنك وشبكة البطاقة وفئتها فقط لمطابقة عروض الفنادق. لا نحفظ رقم البطاقة أو CVV.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Surface(color = SoftMint, shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Security, null, tint = Emerald); Spacer(Modifier.width(10.dp))
                    Text("المحرك يرسل وصف البطاقة فقط عند البحث عن عرض ينطبق عليك.", color = Ink, fontWeight = FontWeight.Medium)
                }
            }
        }
        if (cards.isEmpty()) item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CreditCardOff, null, tint = Muted, modifier = Modifier.size(34.dp)); Spacer(Modifier.height(9.dp))
                    Text("لم تضف أي بطاقة", fontWeight = FontWeight.Black)
                    Text("أضف نوع البطاقة فقط، وسيخبرك اقتنص إن كان هناك عرض خاص بها.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(cards, key = { it.id }) { card ->
            PaymentCardTile(
                card = card,
                onToggle = { onCardsChange(cards.map { if (it.id == card.id) it.copy(isEnabled = !it.isEnabled) else it }) },
                onDelete = { onCardsChange(cards.filterNot { it.id == card.id }) }
            )
        }
        item {
            OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp)) {
                Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(7.dp)); Text("إضافة نوع بطاقة", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentCardTile(card: UserPaymentCard, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(color = Night, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Sand, shape = RoundedCornerShape(13.dp)) { Icon(Icons.Rounded.CreditCard, null, tint = Night, modifier = Modifier.padding(10.dp)) }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(card.bank, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Text("${card.network} • ${card.tier}", color = Color.White.copy(alpha = .7f))
                }
                Switch(checked = card.isEnabled, onCheckedChange = { onToggle() })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (card.isEnabled) "تدخل في مطابقة العروض" else "متوقفة مؤقتًا", color = if (card.isEnabled) Mint else Color.White.copy(alpha = .55f), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = Color.White.copy(alpha = .72f)) }
            }
        }
    }
}

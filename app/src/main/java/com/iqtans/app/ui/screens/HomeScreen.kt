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
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.SearchRequest
import com.iqtans.app.ui.components.*
import com.iqtans.app.ui.theme.*

@Composable
fun HomeScreen(
    city: String,
    featured: List<HotelDeal>,
    isLive: Boolean,
    onChangeCity: () -> Unit,
    onSearch: (SearchRequest) -> Unit,
    onHotelClick: (HotelDeal) -> Unit
) {
    var hotelQuery by remember { mutableStateOf("") }
    var flexibility by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 44.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(compact = true, modifier = Modifier.weight(1f))
                Surface(onClick = onChangeCity, color = SoftMint, shape = RoundedCornerShape(999.dp)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Emerald, modifier = Modifier.size(17.dp))
                        Text(" $city", fontWeight = FontWeight.Bold, color = Emerald)
                    }
                }
            }
            Spacer(Modifier.height(25.dp))
            Text("لا تدفع قبل أن نبحث لك.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(5.dp))
            Text("نبحث عن السعر الذي لا يظهر لك من أول نظرة.", style = MaterialTheme.typography.bodyLarge, color = Muted)
        }

        if (!isLive) item { DemoModeBanner() }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("ابحث عن فندق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = hotelQuery,
                        onValueChange = { hotelQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("اسم الفندق — اختياري") },
                        leadingIcon = { Icon(Icons.Rounded.Hotel, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(11.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SearchFieldTile(Icons.Rounded.CalendarMonth, "الدخول", "20 سبتمبر", Modifier.weight(1f))
                        SearchFieldTile(Icons.Rounded.CalendarMonth, "الخروج", "23 سبتمبر", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    SearchFieldTile(Icons.Rounded.Group, "النزلاء", "شخصان • غرفة واحدة", Modifier.fillMaxWidth())
                    Spacer(Modifier.height(15.dp))
                    Text("مرونة التواريخ", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "ثابتة", 1 to "± يوم", 3 to "± 3 أيام").forEach { (days, label) ->
                            FilterChip(
                                selected = flexibility == days,
                                onClick = { flexibility = days },
                                label = { Text(label) },
                                leadingIcon = if (flexibility == days) {{ Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }} else null
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onSearch(
                                SearchRequest(
                                    city = city,
                                    hotelQuery = hotelQuery,
                                    checkIn = "20 سبتمبر",
                                    checkOut = "23 سبتمبر",
                                    guests = 2,
                                    rooms = 1,
                                    flexibilityDays = flexibility
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ابدأ الاقتناص", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            SectionTitle("أشياء قد لا تعرفها", "اقتنص يبحث عنها تلقائيًا قبل أن يدفعك للحجز.")
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                DiscoveryRow(Icons.Rounded.WorkspacePremium, "عضوية مجانية", "قد تجعل الموقع الرسمي أرخص من كل المنصات.")
                DiscoveryRow(Icons.Rounded.CreditCard, "بطاقتك قد تغيّر الفائز", "نطابق نوع البطاقة وشروط العرض قبل احتساب التوفير.")
                DiscoveryRow(Icons.Rounded.PhoneAndroid, "سعر التطبيق", "بعض الأسعار تظهر داخل التطبيق فقط ولا تظهر على الموقع.")
                DiscoveryRow(Icons.Rounded.EventRepeat, "يوم قريب أوفر", "نقترح تاريخًا قريبًا فقط إذا كان الفرق يستحق التغيير.")
            }
        }

        item { SectionTitle("فرص قوية الآن", "نماذج للواجهة إلى أن تُربط الأسعار الحية.") }
        if (featured.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Icon(Icons.Rounded.TravelExplore, null, tint = Emerald)
                        Spacer(Modifier.height(8.dp))
                        Text("لا توجد بيانات تجريبية لهذه المدينة بعد", fontWeight = FontWeight.Bold)
                        Text("بعد ربط مزود حي ستظهر الفنادق والأسعار الحقيقية هنا.", color = Muted)
                    }
                }
            }
        } else {
            items(featured, key = { it.id }) { hotel ->
                HotelDealCard(hotel = hotel, onClick = { onHotelClick(hotel) })
            }
        }
    }
}

@Composable
private fun DiscoveryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = SoftMint, shape = RoundedCornerShape(13.dp)) {
                Icon(icon, null, tint = Emerald, modifier = Modifier.padding(10.dp).size(21.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}

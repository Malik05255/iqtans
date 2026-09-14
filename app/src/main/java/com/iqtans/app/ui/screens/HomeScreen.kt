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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class PickerTarget { CHECK_IN, CHECK_OUT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    city: String,
    featured: List<HotelDeal>,
    isLive: Boolean,
    onChangeCity: () -> Unit,
    onSearch: (SearchRequest) -> Unit,
    onHotelClick: (HotelDeal) -> Unit
) {
    val today = remember { LocalDate.now() }
    var hotelQuery by remember { mutableStateOf("") }
    var flexibility by remember { mutableIntStateOf(1) }
    var checkIn by remember { mutableStateOf(today.plusDays(7)) }
    var checkOut by remember { mutableStateOf(today.plusDays(10)) }
    var guests by remember { mutableIntStateOf(2) }
    var rooms by remember { mutableIntStateOf(1) }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    pickerTarget?.let { target ->
        val current = if (target == PickerTarget.CHECK_IN) checkIn else checkOut
        val state = rememberDatePickerState(initialSelectedDateMillis = current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { pickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val date = state.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    if (date != null && !date.isBefore(today)) {
                        if (target == PickerTarget.CHECK_IN) {
                            checkIn = date
                            if (!checkOut.isAfter(checkIn)) checkOut = checkIn.plusDays(1)
                        } else if (date.isAfter(checkIn)) checkOut = date
                    }
                    pickerTarget = null
                }) { Text("اختيار") }
            },
            dismissButton = { TextButton(onClick = { pickerTarget = null }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }

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
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("ابحث عن فندق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = hotelQuery, onValueChange = { hotelQuery = it }, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("اسم الفندق — اختياري") }, leadingIcon = { Icon(Icons.Rounded.Hotel, null) },
                        singleLine = true, shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(11.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SearchFieldTile(Icons.Rounded.CalendarMonth, "الدخول", displayDate(checkIn), Modifier.weight(1f)) { pickerTarget = PickerTarget.CHECK_IN }
                        SearchFieldTile(Icons.Rounded.CalendarMonth, "الخروج", displayDate(checkOut), Modifier.weight(1f)) { pickerTarget = PickerTarget.CHECK_OUT }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CounterTile("النزلاء", guests, 1, 12, Icons.Rounded.Group, Modifier.weight(1f)) { guests = it }
                        CounterTile("الغرف", rooms, 1, 8, Icons.Rounded.MeetingRoom, Modifier.weight(1f)) { rooms = it }
                    }
                    Spacer(Modifier.height(15.dp))
                    Text("مرونة التواريخ", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "ثابتة", 1 to "± يوم", 3 to "± 3 أيام").forEach { (days, label) ->
                            FilterChip(selected = flexibility == days, onClick = { flexibility = days }, label = { Text(label) },
                                leadingIcon = if (flexibility == days) {{ Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }} else null)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onSearch(SearchRequest(city, hotelQuery, checkIn.toString(), checkOut.toString(), guests, rooms, flexibility)) },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Search, null); Spacer(Modifier.width(8.dp)); Text("ابدأ الاقتناص", fontWeight = FontWeight.Black)
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

        item { SectionTitle("فرص قوية الآن", if (isLive) "نتائج من محرك اقتنص الحي." else "نماذج للواجهة إلى أن تُربط مفاتيح المزودين الحية.") }
        if (featured.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Icon(Icons.Rounded.TravelExplore, null, tint = Emerald); Spacer(Modifier.height(8.dp))
                        Text("لا توجد نتائج لهذه المدينة بعد", fontWeight = FontWeight.Bold)
                        Text("عند توفر مصدر حي ستظهر الفنادق والأسعار هنا.", color = Muted)
                    }
                }
            }
        } else items(featured, key = { it.id }) { hotel -> HotelDealCard(hotel = hotel, onClick = { onHotelClick(hotel) }) }
    }
}

private fun displayDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMM", Locale("ar", "SA")))

@Composable
private fun CounterTile(label: String, value: Int, min: Int, max: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onChange: (Int) -> Unit) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Emerald, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(6.dp)); Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min) { Icon(Icons.Rounded.Remove, null) }
                Text(value.toString(), fontWeight = FontWeight.Black)
                IconButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max) { Icon(Icons.Rounded.Add, null) }
            }
        }
    }
}

@Composable
private fun DiscoveryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = SoftMint, shape = RoundedCornerShape(13.dp)) { Icon(icon, null, tint = Emerald, modifier = Modifier.padding(10.dp).size(21.dp)) }
            Spacer(Modifier.width(11.dp)); Column { Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge); Text(text, style = MaterialTheme.typography.bodyMedium, color = Muted) }
        }
    }
}

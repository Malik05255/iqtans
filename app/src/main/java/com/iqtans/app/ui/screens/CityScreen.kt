package com.iqtans.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iqtans.app.ui.components.BrandMark
import com.iqtans.app.ui.theme.Emerald
import com.iqtans.app.ui.theme.Muted
import com.iqtans.app.ui.theme.SoftMint

@Composable
fun CityScreen(
    cities: List<String>,
    onCitySelected: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 54.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BrandMark()
                Spacer(Modifier.height(36.dp))
                Text("من أين نبدأ الاقتناص؟", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text(
                    "اختر المدينة أولًا. بعدها نبحث في طرق الحجز والعروض والعضويات والبطاقات لنصل لأقل تكلفة قابلة للتنفيذ.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )
                Spacer(Modifier.height(18.dp))
            }
            items(cities) { city ->
                Card(
                    onClick = { onCitySelected(city) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = SoftMint, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                            Icon(
                                if (city == "مكة المكرمة") Icons.Rounded.LocationOn else Icons.Rounded.LocationCity,
                                contentDescription = null,
                                tint = Emerald,
                                modifier = Modifier.padding(11.dp).size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        Text(city, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = Muted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

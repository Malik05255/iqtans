package com.iqtans.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqtans.app.data.BackendSearchClient
import com.iqtans.app.data.DealRepository
import com.iqtans.app.data.LocalPreferences
import com.iqtans.app.domain.*
import com.iqtans.app.ui.screens.*
import com.iqtans.app.ui.theme.Emerald
import com.iqtans.app.ui.theme.Muted

private enum class AppScreen { CITY, HOME, RESULTS, HOTEL, DEAL, CARDS, WATCHLIST }

@Composable
fun IqtansApp(repository: DealRepository, liveClient: BackendSearchClient? = null, storage: LocalPreferences? = null) {
    val cities = remember { repository.cities() }
    var screenName by rememberSaveable { mutableStateOf(AppScreen.CITY.name) }; val screen = AppScreen.valueOf(screenName)
    var request by remember { mutableStateOf(SearchRequest(city = cities.firstOrNull().orEmpty())) }
    var selectedHotelId by rememberSaveable { mutableStateOf<String?>(null) }; var selectedOfferId by rememberSaveable { mutableStateOf<String?>(null) }
    val hotelSnapshots = remember { mutableStateMapOf<String, HotelDeal>() }
    var cards by remember { mutableStateOf(storage?.loadCards().orEmpty()) }
    var watches by remember { mutableStateOf(storage?.loadWatches().orEmpty()) }
    var liveEnvelope by remember { mutableStateOf<LiveSearchEnvelope?>(null) }; var loading by remember { mutableStateOf(false) }; var loadError by remember { mutableStateOf<String?>(null) }
    val fallbackResults = remember(request) { repository.search(request) }

    LaunchedEffect(cards) { storage?.saveCards(cards) }
    LaunchedEffect(watches) { storage?.saveWatches(watches) }
    LaunchedEffect(request, cards, liveClient) {
        if (liveClient == null) { liveEnvelope = null; loading = false; return@LaunchedEffect }
        loading = true; loadError = null
        try { liveEnvelope = liveClient.search(request, cards) } catch (t: Throwable) { loadError = t.message ?: "تعذر الاتصال بمحرك اقتنص"; liveEnvelope = null } finally { loading = false }
    }

    val liveResults = liveEnvelope?.hotels.orEmpty(); val results = if (liveResults.isNotEmpty()) liveResults else fallbackResults
    val isLive = liveEnvelope?.mode == LiveSearchMode.LIVE || liveEnvelope?.mode == LiveSearchMode.PARTIAL
    val selectedHotel = selectedHotelId?.let { id -> hotelSnapshots[id] ?: results.firstOrNull { it.id == id } ?: repository.getHotel(id) }
    val selectedOffer = selectedHotel?.offers?.firstOrNull { it.id == selectedOfferId }
    fun selectHotel(hotel: HotelDeal) { hotelSnapshots[hotel.id] = hotel; selectedHotelId = hotel.id; selectedOfferId = null; screenName = AppScreen.HOTEL.name }
    fun watchId(hotel: HotelDeal) = "${hotel.id}|${request.checkIn}|${request.checkOut}|${request.guests}|${request.rooms}"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background, bottomBar = { if (screen in setOf(AppScreen.HOME, AppScreen.RESULTS, AppScreen.CARDS, AppScreen.WATCHLIST)) IqtansBottomBar(selected = screen, onSelect = { screenName = it.name }) }) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (screen) {
                    AppScreen.CITY -> CityScreen(cities = cities, onCitySelected = { city -> liveEnvelope = null; request = request.copy(city = city, hotelQuery = ""); screenName = AppScreen.HOME.name })
                    AppScreen.HOME -> HomeScreen(city = request.city, featured = results.take(3), isLive = isLive, onChangeCity = { screenName = AppScreen.CITY.name }, onSearch = { request = it; screenName = AppScreen.RESULTS.name }, onHotelClick = ::selectHotel)
                    AppScreen.RESULTS -> ResultsScreen(request = request, hotels = results, isLive = isLive, onBack = { screenName = AppScreen.HOME.name }, onHotelClick = ::selectHotel)
                    AppScreen.HOTEL -> {
                        val hotel = selectedHotel
                        if (hotel == null) MissingSelectionScreen("لم أجد الفندق المحدد.") { screenName = AppScreen.RESULTS.name }
                        else {
                            val key = watchId(hotel); val watched = watches.any { it.id == key }
                            HotelDetailScreen(hotel, watched, { screenName = AppScreen.RESULTS.name }, {
                                watches = if (watched) watches.filterNot { it.id == key } else watches + PriceWatch(
                                    id = key, hotelId = hotel.id, hotelName = hotel.name, city = request.city, checkIn = request.checkIn, checkOut = request.checkOut,
                                    guests = request.guests, rooms = request.rooms, lastSeenPrice = hotel.bestOffer.finalAmount, currency = hotel.bestOffer.currency, savedAt = System.currentTimeMillis()
                                )
                            }, { selectedOfferId = it.id; screenName = AppScreen.DEAL.name })
                        }
                    }
                    AppScreen.DEAL -> {
                        val hotel = selectedHotel; val offer = selectedOffer
                        if (hotel == null || offer == null) MissingSelectionScreen("لم أجد تفاصيل العرض المحدد.") { screenName = AppScreen.HOTEL.name }
                        else DealDetailScreen(hotelName = hotel.name, offer = offer, onBack = { screenName = AppScreen.HOTEL.name }, onVerify = liveClient?.let { client -> { client.verify(request, cards, offer) } })
                    }
                    AppScreen.CARDS -> CardsScreen(cards = cards, onCardsChange = { cards = it })
                    AppScreen.WATCHLIST -> WatchlistScreen(watches = watches, onCheck = { watch -> request = watch.toSearchRequest(); screenName = AppScreen.RESULTS.name }, onDelete = { watch -> watches = watches.filterNot { it.id == watch.id } })
                }
                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                val status = loadError ?: liveEnvelope?.warnings?.firstOrNull()
                if (status != null && screen != AppScreen.CITY) Surface(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 18.dp, end = 18.dp), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer, shadowElevation = 2.dp) { Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp) }
            }
        }
    }
}

@Composable private fun IqtansBottomBar(selected: AppScreen, onSelect: (AppScreen) -> Unit) { NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) { BottomDestination.entries.forEach { item -> NavigationBarItem(selected = selected == item.screen, onClick = { onSelect(item.screen) }, icon = { Icon(item.icon, item.label) }, label = { Text(item.label, fontSize = 11.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Emerald, selectedTextColor = Emerald, indicatorColor = Emerald.copy(alpha = .10f), unselectedIconColor = Muted, unselectedTextColor = Muted)) } } }
private data class BottomDestination(val screen: AppScreen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) { companion object { val entries = listOf(BottomDestination(AppScreen.HOME,"الرئيسية",Icons.Rounded.Home),BottomDestination(AppScreen.RESULTS,"النتائج",Icons.Rounded.Search),BottomDestination(AppScreen.CARDS,"بطاقاتي",Icons.Rounded.CreditCard),BottomDestination(AppScreen.WATCHLIST,"المراقبة",Icons.Rounded.Notifications)) } }
@Composable private fun MissingSelectionScreen(text: String, onBack: () -> Unit) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text, style = MaterialTheme.typography.titleMedium); Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) { Text("رجوع") } } } }

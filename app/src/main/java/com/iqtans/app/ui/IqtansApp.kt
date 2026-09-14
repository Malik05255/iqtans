package com.iqtans.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.iqtans.app.data.DealRepository
import com.iqtans.app.domain.DealOffer
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.SearchRequest
import com.iqtans.app.ui.screens.*
import com.iqtans.app.ui.theme.Canvas
import com.iqtans.app.ui.theme.Emerald
import com.iqtans.app.ui.theme.Muted

private enum class AppScreen {
    CITY,
    HOME,
    RESULTS,
    HOTEL,
    DEAL,
    CARDS,
    WATCHLIST
}

@Composable
fun IqtansApp(repository: DealRepository) {
    val cities = remember { repository.cities() }
    var screenName by rememberSaveable { mutableStateOf(AppScreen.CITY.name) }
    val screen get() = AppScreen.valueOf(screenName)

    var request by remember {
        mutableStateOf(
            SearchRequest(city = cities.firstOrNull().orEmpty())
        )
    }
    var selectedHotelId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedOfferId by rememberSaveable { mutableStateOf<String?>(null) }
    val watchedIds = remember { mutableStateListOf<String>() }

    val results = remember(request) { repository.search(request) }
    val selectedHotel = selectedHotelId?.let(repository::getHotel)
    val selectedOffer = selectedHotel?.offers?.firstOrNull { it.id == selectedOfferId }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (screen in setOf(AppScreen.HOME, AppScreen.RESULTS, AppScreen.CARDS, AppScreen.WATCHLIST)) {
                    IqtansBottomBar(
                        selected = screen,
                        onSelect = { destination -> screenName = destination.name }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (screen) {
                    AppScreen.CITY -> CityScreen(
                        cities = cities,
                        onCitySelected = { city ->
                            request = request.copy(city = city, hotelQuery = "")
                            screenName = AppScreen.HOME.name
                        }
                    )

                    AppScreen.HOME -> HomeScreen(
                        city = request.city,
                        featured = results.take(3),
                        isLive = repository.isLive,
                        onChangeCity = { screenName = AppScreen.CITY.name },
                        onSearch = { newRequest ->
                            request = newRequest
                            screenName = AppScreen.RESULTS.name
                        },
                        onHotelClick = { hotel ->
                            selectedHotelId = hotel.id
                            selectedOfferId = null
                            screenName = AppScreen.HOTEL.name
                        }
                    )

                    AppScreen.RESULTS -> ResultsScreen(
                        request = request,
                        hotels = results,
                        isLive = repository.isLive,
                        onBack = { screenName = AppScreen.HOME.name },
                        onHotelClick = { hotel ->
                            selectedHotelId = hotel.id
                            selectedOfferId = null
                            screenName = AppScreen.HOTEL.name
                        }
                    )

                    AppScreen.HOTEL -> {
                        val hotel = selectedHotel
                        if (hotel == null) {
                            MissingSelectionScreen(
                                text = "لم أجد الفندق المحدد.",
                                onBack = { screenName = AppScreen.RESULTS.name }
                            )
                        } else {
                            HotelDetailScreen(
                                hotel = hotel,
                                watched = hotel.id in watchedIds,
                                onBack = { screenName = AppScreen.RESULTS.name },
                                onToggleWatch = {
                                    if (hotel.id in watchedIds) watchedIds.remove(hotel.id)
                                    else watchedIds.add(hotel.id)
                                },
                                onOfferClick = { offer ->
                                    selectedOfferId = offer.id
                                    screenName = AppScreen.DEAL.name
                                }
                            )
                        }
                    }

                    AppScreen.DEAL -> {
                        val hotel = selectedHotel
                        val offer = selectedOffer
                        if (hotel == null || offer == null) {
                            MissingSelectionScreen(
                                text = "لم أجد تفاصيل العرض المحدد.",
                                onBack = { screenName = AppScreen.HOTEL.name }
                            )
                        } else {
                            DealDetailScreen(
                                hotelName = hotel.name,
                                offer = offer,
                                onBack = { screenName = AppScreen.HOTEL.name }
                            )
                        }
                    }

                    AppScreen.CARDS -> CardsScreen()

                    AppScreen.WATCHLIST -> WatchlistScreen(
                        hotels = watchedIds.mapNotNull(repository::getHotel),
                        onHotelClick = { hotel ->
                            selectedHotelId = hotel.id
                            selectedOfferId = null
                            screenName = AppScreen.HOTEL.name
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IqtansBottomBar(
    selected: AppScreen,
    onSelect: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomDestination.entries.forEach { item ->
            NavigationBarItem(
                selected = selected == item.screen,
                onClick = { onSelect(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Emerald,
                    selectedTextColor = Emerald,
                    indicatorColor = Emerald.copy(alpha = .10f),
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}

private data class BottomDestination(
    val screen: AppScreen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    companion object {
        val entries = listOf(
            BottomDestination(AppScreen.HOME, "الرئيسية", Icons.Rounded.Home),
            BottomDestination(AppScreen.RESULTS, "النتائج", Icons.Rounded.Search),
            BottomDestination(AppScreen.CARDS, "بطاقاتي", Icons.Rounded.CreditCard),
            BottomDestination(AppScreen.WATCHLIST, "المراقبة", Icons.Rounded.Notifications)
        )
    }
}

@Composable
private fun MissingSelectionScreen(
    text: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(text, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
                Text("رجوع")
            }
        }
    }
}

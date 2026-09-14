package com.iqtans.app.data

import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.SearchRequest

interface DealRepository {
    val isLive: Boolean
    fun cities(): List<String>
    fun search(request: SearchRequest): List<HotelDeal>
    fun getHotel(id: String): HotelDeal?
}

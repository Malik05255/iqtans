package com.iqtans.app.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

fun formatMoney(value: Double): String {
    val rounded = round(value * 100.0) / 100.0
    if (abs(rounded - rounded.toLong()) < 0.000001) return rounded.toLong().toString()
    val symbols = DecimalFormatSymbols(Locale.US)
    return DecimalFormat("#,##0.00", symbols).format(rounded)
}

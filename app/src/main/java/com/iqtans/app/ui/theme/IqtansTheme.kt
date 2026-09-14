package com.iqtans.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Night = Color(0xFF071C18)
val Emerald = Color(0xFF0B6B52)
val Mint = Color(0xFF12A67D)
val Sand = Color(0xFFF2C96D)
val Canvas = Color(0xFFF6F7F3)
val Ink = Color(0xFF10211D)
val Muted = Color(0xFF68766F)
val SoftMint = Color(0xFFE5F5EF)
val SoftSand = Color(0xFFFFF5D9)
val Danger = Color(0xFFB94A48)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    secondary = Sand,
    onSecondary = Night,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF1ED),
    onSurfaceVariant = Muted,
    outline = Color(0xFFD4DDD7),
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Night,
    secondary = Sand,
    onSecondary = Night,
    background = Color(0xFF061511),
    onBackground = Color(0xFFF3F7F5),
    surface = Color(0xFF0C211B),
    onSurface = Color(0xFFF3F7F5),
    surfaceVariant = Color(0xFF17332A),
    onSurfaceVariant = Color(0xFFBDD0C7),
    outline = Color(0xFF35564A)
)

private val IqtansTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

@Composable
fun IqtansTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = IqtansTypography,
        content = content
    )
}

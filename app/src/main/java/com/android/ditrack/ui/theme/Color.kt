package com.android.ditrack.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val SoftWhite = Color(0xFFF8FCFB)
val SoftBlack = Color(0xFF1E1E1E)
val Blue800 = Color(0xFF1565C0)
val Charcoal = Color(0xFF36454F)

val LightColorScheme = lightColorScheme(
    primary = Blue800,
    secondary = Charcoal,
    tertiary = Color.LightGray,
    background = SoftWhite,
    surface = SoftWhite,
    onPrimary = SoftWhite,
    onBackground = SoftBlack,
    onSurface = SoftBlack,
    outline = Color.Gray,
)
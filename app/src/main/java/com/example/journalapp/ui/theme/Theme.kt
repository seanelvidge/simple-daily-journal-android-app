package com.example.journalapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Sea,
    onPrimary = Color.White,
    secondary = Clay,
    onSecondary = Color.White,
    background = Sand,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Clay,
    onPrimary = Night,
    secondary = Sea,
    onSecondary = Color.White,
    background = Night,
    onBackground = Color(0xFFE8ECEF),
    surface = Color(0xFF151B21),
    onSurface = Color(0xFFE8ECEF),
)

@Composable
fun JournalTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content,
    )
}

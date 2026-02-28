package com.example.reminderht5.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReminderLightColors = lightColorScheme(
    primary = Color(0xFF1B8E6B),
    secondary = Color(0xFF1976D2),
    background = Color(0xFFEAF5F7),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937)
)

private val AppTypography = Typography()

@Composable
fun ReminderHTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReminderLightColors,
        typography = AppTypography,
        content = content
    )
}

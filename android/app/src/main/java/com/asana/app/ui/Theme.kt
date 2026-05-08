package com.asana.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design System: Organic Biophilic — calming lavender + wellness green
val PrimaryPurple    = Color(0xFF8B5CF6)
val SecondaryLavender = Color(0xFFC4B5FD)
val AccentGreen      = Color(0xFF059669)
val BackgroundCream  = Color(0xFFFAF5FF)
val ForegroundDeep   = Color(0xFF4C1D95)
val MutedLavender    = Color(0xFFEDEFF9)
val BorderLavender   = Color(0xFFEDE9FE)
val TextPrimary      = Color(0xFF1F1235)
val TextSecondary    = Color(0xFF6B7280)
val UserBubble       = Color(0xFF8B5CF6)
val AiBubble         = Color(0xFFEDE9FE)

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryPurple,
    onPrimary        = Color.White,
    secondary        = SecondaryLavender,
    onSecondary      = ForegroundDeep,
    background       = BackgroundCream,
    surface          = Color.White,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    tertiary         = AccentGreen,
)

@Composable
fun AsanaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}

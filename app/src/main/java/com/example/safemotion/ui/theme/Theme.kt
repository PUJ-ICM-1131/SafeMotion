package com.example.safemotion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E1FF),
    onPrimaryContainer = Color(0xFF100066),
    secondary = Color(0xFF5A5B72),
    secondaryContainer = Color(0xFFE0E0F2),
    tertiary = SportOrange,
    tertiaryContainer = Color(0xFFFFDCC2),
    background = LightSurface,
    surface = LightSurface,
    onBackground = Color(0xFF1B1B22),
    onSurface = Color(0xFF1B1B22),
    onSurfaceVariant = Color(0xFF46464F)
)
private val DarkColors = darkColorScheme(
    primary = LightIndigo,
    onPrimary = Color(0xFF1B1878),
    primaryContainer = Color(0xFF3331C0),
    onPrimaryContainer = Color(0xFFE3E1FF),
    secondary = Color(0xFFC4C4DD),
    secondaryContainer = Color(0xFF424356),
    tertiary = LightSportOrange,
    tertiaryContainer = Color(0xFF6F3800),
    background = DarkSurface,
    surface = DarkSurface,
    onBackground = Color(0xFFE5E1E9),
    onSurface = Color(0xFFE5E1E9),
    onSurfaceVariant = Color(0xFFC7C5D0)
)

@Composable
fun SafeMotionTheme(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SafeMotionTypography
    ) {
        Box(modifier = modifier) { content() }
    }
}

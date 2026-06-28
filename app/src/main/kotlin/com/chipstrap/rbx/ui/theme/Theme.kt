package com.chipstrap.rbx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00C853),
    onPrimary = Color(0xFF001C13),
    primaryContainer = Color(0xFF009624),
    onPrimaryContainer = Color(0xFFB4F5C8),
    secondary = Color(0xFFFFC107),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF0E1417),
    onBackground = Color(0xFFE7EAEC),
    surface = Color(0xFF171E22),
    onSurface = Color(0xFFE7EAEC),
    surfaceVariant = Color(0xFF1F272C),
    onSurfaceVariant = Color(0xFF9CA4AB),
    error = Color(0xFFFF5252),
    outline = Color(0xFF2A3338)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF009624),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4F5C8),
    onPrimaryContainer = Color(0xFF00210B),
    secondary = Color(0xFFE0A800),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFAFCFB),
    onBackground = Color(0xFF101416),
    surface = Color(0xFFF0F4F2),
    onSurface = Color(0xFF101416),
    surfaceVariant = Color(0xFFE5EAE8),
    onSurfaceVariant = Color(0xFF3C4744),
    error = Color(0xFFB00020),
    outline = Color(0xFF707880)
)

@Composable
fun ChipstrapTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        content = content
    )
}

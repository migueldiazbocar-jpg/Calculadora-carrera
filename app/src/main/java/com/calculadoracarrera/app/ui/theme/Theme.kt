package com.calculadoracarrera.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RaceRed = Color(0xFFD32F2F)
private val RaceRedLight = Color(0xFFC62828)

private val DarkColors = darkColorScheme(
    primary = RaceRed,
    onPrimary = Color.White,
    background = Color(0xFF2E3236),
    onBackground = Color(0xFFECEFF1),
    surface = Color(0xFF2E3236),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF464B50),
    onSurfaceVariant = Color(0xFFB8BEC3),
    outline = Color(0xFF7D848A),
)

private val LightColors = lightColorScheme(
    primary = RaceRedLight,
    onPrimary = Color.White,
    background = Color(0xFFECEFF1),
    onBackground = Color(0xFF1E2124),
    surface = Color(0xFFECEFF1),
    onSurface = Color(0xFF1E2124),
    surfaceVariant = Color(0xFFD5D9DC),
    onSurfaceVariant = Color(0xFF4A5056),
    outline = Color(0xFF8A9096),
)

@Composable
fun CalculadoraCarreraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

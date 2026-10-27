package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = ForestDark,
    secondary = GoldAccent,
    onSecondary = Color(0xFF1E1700),
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF433400),
    tertiary = ForestDark,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = TextMain,
    surface = CardSurface,
    onSurface = TextMain,
    surfaceVariant = Color(0xFFF3EAD8),
    onSurfaceVariant = TextMuted,
    outline = BorderParchment,
    outlineVariant = Color(0xFFE2D7C0),
    error = RedError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = GoldAccent,
    onSecondary = Color(0xFF281E00),
    secondaryContainer = Color(0xFF4C3800),
    onSecondaryContainer = GoldLight,
    tertiary = Color(0xFF6EE7B7),
    onTertiary = Color(0xFF003824),
    background = DarkBackground,
    onBackground = DarkTextMain,
    surface = DarkSurface,
    onSurface = DarkTextMain,
    surfaceVariant = Color(0xFF1C2C24),
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = Color(0xFF2F4639),
    error = Color(0xFFF87171),
    onError = Color.Black
)

@Composable
fun DDQuranTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

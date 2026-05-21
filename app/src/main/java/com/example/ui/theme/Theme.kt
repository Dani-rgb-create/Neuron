package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GentlePrimary,
    primaryContainer = CardSlateBg,
    onPrimaryContainer = CreamWhiteText,
    secondary = GentleSecondary,
    tertiary = GentleTertiary,
    background = SoftBgDark,
    surface = CardSlateBg,
    surfaceVariant = DeepSlateBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CreamWhiteText,
    onSurface = CreamWhiteText,
    onSurfaceVariant = CreamWhiteText,
    outline = CardSlateBg
)

private val LightColorScheme = lightColorScheme(
    primary = ImmersivePrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = SecondaryOrange,
    tertiary = TertiarySoftGreen,
    background = ImmersiveBg,
    surface = ImmersiveSurface,
    surfaceVariant = ImmersiveSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ImmersiveOnBg,
    onSurface = ImmersiveOnBg,
    onSurfaceVariant = ImmersiveMediumText,
    outline = ImmersiveOutline,
    outlineVariant = ImmersiveOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

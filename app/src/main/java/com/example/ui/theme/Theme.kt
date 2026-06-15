package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangeAccent,
    secondary = SlateBlue,
    tertiary = GoldYellow,
    background = NavyDark,
    surface = NavyMedium,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = NavyDark,
    onBackground = TextLightMain,
    onSurface = TextLightMain,
    surfaceVariant = NavyLight,
    onSurfaceVariant = TextLightSec,
    outline = SlateBlue
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeAccent,
    secondary = SlateBlue,
    tertiary = GoldYellow,
    background = AsphaltColor,
    surface = CardLightColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TextDarkMain,
    onBackground = TextDarkMain,
    onSurface = TextDarkMain,
    surfaceVariant = AsphaltColor,
    onSurfaceVariant = TextDarkSec,
    outline = NavyLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable system dynamic layout colors to preserve our cohesive dashboard aesthetic of orange and dark navy.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

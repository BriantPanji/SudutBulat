package com.sudutcorp.sudutbulat.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = SBColor.Primary,
    onPrimary = Color.White,
    primaryContainer = SBColor.Primary_100,
    onPrimaryContainer = SBColor.Primary_700,

    secondary = SBColor.Secondary_500,
    onSecondary = Color.White,
    secondaryContainer = SBColor.Secondary_100,
    onSecondaryContainer = SBColor.Secondary_700,

    tertiary = SBColor.Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = SBColor.Tertiary_100,
    onTertiaryContainer = SBColor.Tertiary_700,

    background = Color.White,
    onBackground = SBColor.Grey_700,
    surface = Color.White,
    onSurface = SBColor.Grey_700,
    surfaceVariant = SBColor.Grey_100,
    onSurfaceVariant = SBColor.Grey_500,

    outline = SBColor.Grey_300,
    outlineVariant = SBColor.Grey_200,

    error = SBColor.Primary_550,
    onError = Color.White
)

@Composable
fun SudutBulatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
package com.secverge.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SECVergeDarkColorScheme = darkColorScheme(
    primary = Cobalt,
    secondary = Cyan,
    tertiary = Violet,
    background = MidnightCanvas,
    surface = MidnightSurface,
    surfaceVariant = MidnightSurfaceHigh,
    onPrimary = TextPrimary,
    onSecondary = MidnightCanvas,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    onError = TextPrimary
)

@Composable
fun SECVergeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SECVergeDarkColorScheme,
        typography = SECVergeTypography,
        content = content
    )
}

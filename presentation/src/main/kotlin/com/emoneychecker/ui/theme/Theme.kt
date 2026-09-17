package com.emoneychecker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmoneyColors.Mandiri,
    background = EmoneyColors.Background,
    surface = EmoneyColors.SurfaceLevel1,
    onPrimary = EmoneyColors.Background,
    onBackground = EmoneyColors.TextPrimary,
    onSurface = EmoneyColors.TextPrimary
)

@Composable
fun EmoneyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

package com.example.spendwise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary   = Color(0xFF1A5C3A)
private val GreenSecondary = Color(0xFF4CAF82)
private val GreenContainer = Color(0xFFE8F5EE)

private val LightColors = lightColorScheme(
    primary          = GreenPrimary,
    secondary        = GreenSecondary,
    primaryContainer = GreenContainer,
)

private val DarkColors = darkColorScheme(
    primary          = GreenSecondary,
    secondary        = GreenPrimary,
)

@Composable
fun SpendWiseTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}

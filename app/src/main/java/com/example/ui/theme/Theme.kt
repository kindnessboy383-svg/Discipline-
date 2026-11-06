package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonLavender,
    onPrimary = Color(0xFF1E0A3C),
    primaryContainer = DeepPurple,
    onPrimaryContainer = LavenderGlow,
    secondary = NeonPurple,
    onSecondary = Color(0xFF1E0A3C),
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = LavenderGlow,
    tertiary = TechCyan,
    onTertiary = Color(0xFF003649),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF252136),
    error = PriorityHigh,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true for the signature premium dark theme
    dynamicColor: Boolean = false, // Keep signature neon lavender branding consistent
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), // Light purple for dark theme
    secondary = Color(0xFFCCC2DC),
    tertiary = GoldAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimary, // Color(0xFF6750A4)
    secondary = SlateSecondary, // Color(0xFF4F378B)
    tertiary = GoldAccent,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun Modifier.frostedCard(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)
): Modifier {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0x991C1236) else Color(0xCCFFFFFF)
    val borderCol = if (isDark) Color(0x33FFFFFF) else Color(0x66FFFFFF)
    return this
        .background(bg, shape)
        .border(1.dp, borderCol, shape)
}

@Composable
fun FrostedBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0C091A),
                Color(0xFF160E28),
                Color(0xFF080612)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF3F2F8),
                Color(0xFFEADDFF),
                Color(0xFFF3F2F8)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (isDark) {
                // Glow 1: Top-Right (Purple)
                drawCircle(
                    color = Color(0x2A8A2BE2),
                    radius = w * 0.45f,
                    center = Offset(w * 0.85f, h * 0.18f)
                )
                // Glow 2: Bottom-Left (Blue)
                drawCircle(
                    color = Color(0x1C00FFFF),
                    radius = w * 0.55f,
                    center = Offset(w * 0.15f, h * 0.82f)
                )
            } else {
                // Glow 1: Top-Right (Light Purple)
                drawCircle(
                    color = Color(0x4DCEBCFF),
                    radius = w * 0.55f,
                    center = Offset(w * 0.95f, h * 0.12f)
                )
                // Glow 2: Bottom-Left (Soft Pink/Rose)
                drawCircle(
                    color = Color(0x36FFD8E4),
                    radius = w * 0.65f,
                    center = Offset(w * 0.05f, h * 0.78f)
                )
            }
        }
        content()
    }
}

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


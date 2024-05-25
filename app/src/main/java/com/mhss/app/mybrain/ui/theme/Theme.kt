package com.mhss.app.mybrain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.koin.compose.KoinContext

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    surface = DarkGray,
    background = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
    surfaceTint = Color.Transparent,
    surfaceVariant = DarkGray
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = Color.White,
    surfaceTint = Color.Transparent,
    surfaceVariant = Color.White
)

@Composable
fun MyBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontFamily: FontFamily = Rubik,
    content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    val typography = getTypography(fontFamily)
    KoinContext {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = Shapes,
            content = content
        )
    }
}
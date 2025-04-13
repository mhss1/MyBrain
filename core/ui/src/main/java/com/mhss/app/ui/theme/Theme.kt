package com.mhss.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    surface = DarkGray,
    background = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
    surfaceTint = DarkGray,
    surfaceVariant = DarkGray,
    surfaceContainerHighest = DarkGray,
    surfaceContainerLow = DarkGray,
    surfaceContainerLowest = DarkGray,
    surfaceContainer = DarkGray,
    surfaceContainerHigh = DarkGray,
    surfaceDim = DarkGray,
    surfaceBright = DarkGray
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = Color.White,
    surfaceTint = Color.White,
    surfaceVariant = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceDim = Color.White,
    surfaceBright = Color.White
)

@Composable
fun MyBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean = false,
    fontFamily: FontFamily = Rubik,
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    val typography = getTypography(fontFamily, fontSizeScale)
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
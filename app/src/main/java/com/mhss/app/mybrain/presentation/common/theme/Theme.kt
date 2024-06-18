package com.mhss.app.mybrain.presentation.common.theme

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
import org.koin.compose.KoinContext

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
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
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = Color.White,
    surfaceTint = Color.Transparent,
    surfaceVariant = Color.White
)

@Composable
fun MyBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean,
    fontFamily: FontFamily = Rubik,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme){
            dynamicDarkColorScheme(context)
        } else dynamicLightColorScheme(context)
    } else if (darkTheme) {
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
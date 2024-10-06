package com.mhss.app.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProviders
import com.mhss.app.ui.theme.DarkGray
import com.mhss.app.ui.theme.LightGray

val widgetDarkColorScheme = darkColorScheme(
    secondaryContainer = Color(0xFF090909),
    onSecondary = Color(0xFF1A1A1A),
    onSecondaryContainer = Color.White,
    secondary = Color.LightGray
)
val widgetLightColorScheme = darkColorScheme(
    secondaryContainer = Color.White,
    onSecondary = LightGray,
    onSecondaryContainer = Color.Black,
    secondary = DarkGray
)

@Composable
fun WidgetTheme(colors: ColorProviders, content: @Composable () -> Unit) {
    GlanceTheme(
        colors = colors
    ) {
        content()
    }
}
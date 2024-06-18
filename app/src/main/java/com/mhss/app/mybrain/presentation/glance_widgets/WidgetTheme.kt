package com.mhss.app.mybrain.presentation.glance_widgets

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProviders

val widgetDarkColorScheme = darkColorScheme(
    secondaryContainer = Color(0xFF0C0C0C),
    onSecondary = Color.DarkGray,
    onSecondaryContainer = Color.White,
    secondary = Color.LightGray
)

@Composable
fun WidgetTheme(colors: ColorProviders, content: @Composable () -> Unit) {
    GlanceTheme(
        colors = colors
    ) {
        content()
    }
}
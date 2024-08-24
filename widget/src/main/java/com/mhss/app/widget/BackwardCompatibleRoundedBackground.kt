package com.mhss.app.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import com.mhss.app.ui.R

@Composable
fun GlanceModifier.largeBackgroundBasedOnVersion() =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        background(
            ImageProvider(R.drawable.large_item_rounded_corner_shape),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.secondaryContainer)
        )
    } else {
        background(GlanceTheme.colors.secondaryContainer)
            .cornerRadius(25.dp)
    }

@Composable
fun GlanceModifier.largeInnerBackgroundBasedOnVersion() =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        background(
            imageProvider = ImageProvider(R.drawable.large_inner_item_rounded_corner_shape),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondary)
        )
    } else {
        background(GlanceTheme.colors.onSecondary)
            .cornerRadius(20.dp)
    }

@Composable
fun GlanceModifier.smallBackgroundBasedOnVersion() =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        background(
            imageProvider = ImageProvider(R.drawable.small_item_rounded_corner_shape),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.secondaryContainer)
        )
    } else {
        background(GlanceTheme.colors.secondaryContainer)
            .cornerRadius(16.dp)
    }
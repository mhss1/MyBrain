package com.mhss.app.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.theme.LightBlue
import com.mhss.app.ui.theme.LightOrange
import com.mhss.app.ui.theme.LightPurple
import com.mhss.app.ui.theme.MyBrainTheme


fun gradientBrushColor(
    vararg colorStops: Pair<Float, Color> = arrayOf(
        0f to LightBlue,
        0.65f to LightPurple,
        1f to LightOrange
    )
) = Brush.linearGradient(
    colorStops = colorStops,
    start = Offset.Zero,
    end = Offset.Infinite
)

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun GradientColorPreview() {
    MyBrainTheme(useDynamicColors = false) {
        Box(
            Modifier
                .size(100.dp)
                .background(
                    gradientBrushColor()
                )
        )
    }
}
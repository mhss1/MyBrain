package com.mhss.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.gradientBrushColor
import com.mhss.app.ui.theme.MyBrainTheme

@Composable
fun GlowingBorder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    blur: Dp = 14.dp,
    animationDuration: Int = 800,
) {
    val infiniteTransition = rememberInfiniteTransition("glow")
    val borderWidth by infiniteTransition.animateValue(
        initialValue = 7.dp,
        targetValue = 14.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBorder"
    )
    val cornerRadiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    val gradientBorder = remember { gradientBrushColor() }
    val rectCornerRadius = remember { CornerRadius(cornerRadiusPx, cornerRadiusPx) }

    Box(
        modifier = modifier
            .blur(blur)
            .padding(innerPadding)
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawRoundRect(
                    brush = gradientBorder,
                    size = size,
                    cornerRadius = rectCornerRadius,
                    style = Stroke(width = borderWidth.toPx())
                )
            }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlowingBordersPreview() {
    MyBrainTheme {
        Surface {
            GlowingBorder(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
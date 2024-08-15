package com.mhss.app.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.LightPurple
import com.mhss.app.ui.theme.DarkOrange

fun DrawScope.drawAiGradientRadials(
    background: Color,
    backgroundAlpha: Float = 0.75f,
    radius: Float = size.maxDimension * 0.8f
) {
    drawRect(background, Offset.Zero, size)
    drawGradientRadial(
        background
            .copy(alpha = backgroundAlpha)
            .compositeOver(Blue),
        Offset(0f, size.height * 0.9f),
        radius
    )
    drawGradientRadial(
        background
            .copy(alpha = backgroundAlpha)
            .compositeOver(DarkOrange),
        Offset(
            size.width * 1.1f,
            size.height
        ),
        radius
    )
    drawGradientRadial(
        background
            .copy(alpha = backgroundAlpha)
            .compositeOver(LightPurple),
        Offset(
            size.width * 1.1f,
            size.height * .1f,
        ),
        radius
    )
}

fun DrawScope.drawGradientRadial(
    color: Color,
    center: Offset,
    radius: Float = size.maxDimension * 0.75f
) = drawRect(
    brush = Brush.radialGradient(
        colors = listOf(
            color,
            Color.Transparent
        ),
        center = center,
        radius = radius,
    )
)
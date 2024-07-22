package com.mhss.app.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.app.R
import com.mhss.app.ui.gradientBrushColor
import com.mhss.app.ui.theme.LightBlue
import com.mhss.app.ui.theme.LightOrange
import com.mhss.app.ui.theme.LightPurple
import dev.jeziellago.compose.markdowntext.MarkdownText
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@Composable
fun AiResultSheet(
    modifier: Modifier = Modifier,
    loading: Boolean,
    result: String?,
    error: String?,
    onReplaceClick: () -> Unit,
    onAddToNoteClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val backgroundGradient = remember {
        gradientBrushColor(
            0f to surfaceVariant,
            0.4f to surfaceVariant.copy(alpha = 0.8f).compositeOver(LightBlue),
            0.7f to surfaceVariant.copy(alpha = 0.8f).compositeOver(LightPurple),
            1f to surfaceVariant.copy(alpha = 0.8f).compositeOver(LightOrange)
        )
    }
    Card(
        modifier = modifier
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(enabled = false) {},
        shape = SquircleShape(
            radius = 42.dp,
            cornerSmoothing = CornerSmoothing.Medium
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 120.dp)
                .drawBehind {
                    rotate(if (loading) angle else 0f) {
                        drawCircle(backgroundGradient, radius = size.maxDimension / 1.5f)
                    }
                }
                .fillMaxWidth()
                .animateContentSize(
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (result != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    MarkdownText(
                        markdown = result,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                AiResultActions(
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp),
                    onCopyClick = onCopyClick,
                    onReplaceClick = onReplaceClick,
                    onAddToNoteClick = onAddToNoteClick
                )
            }
            if (error != null) {
                MarkdownText(
                    markdown = error,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun AiResultActions(
    modifier: Modifier = Modifier,
    onCopyClick: () -> Unit,
    onReplaceClick: () -> Unit,
    onAddToNoteClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AiResultAction(
            textRes = R.string.copy,
            iconRes = R.drawable.ic_copy,
            onClick = onCopyClick
        )
        AiResultAction(
            textRes = R.string.replace,
            iconRes = R.drawable.ic_replace,
            onClick = onReplaceClick
        )
        AiResultAction(
            textRes = R.string.add_to_note,
            iconRes = R.drawable.ic_add_note,
            onClick = onAddToNoteClick
        )
    }
}

@Composable
private fun RowScope.AiResultAction(
    textRes: Int,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .weight(1f)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = textRes),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


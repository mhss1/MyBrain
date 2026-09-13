package com.mhss.app.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.domain.model.DiaryChartPoint
import com.mhss.app.domain.model.Mood
import com.mhss.app.ui.Res
import com.mhss.app.ui.mood_during_month
import com.mhss.app.ui.mood_during_year
import com.mhss.app.ui.mood_flow
import com.mhss.app.ui.no_data_yet
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoodFlowChart(
    entries: List<DiaryChartPoint>,
    monthly: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.mood_flow),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mostFrequentMood by remember(entries) {
                    derivedStateOf {
                        // if multiple ones with the same frequency, return the most positive one
                        val entriesGrouped = entries
                            .groupBy { it.mood }
                        val max = entriesGrouped.maxOfOrNull { it.value.size }
                        entriesGrouped
                            .filter { it.value.size == max }
                            .maxByOrNull {
                                it.key.value
                            }?.key ?: Mood.OKAY
                    }
                }
                val moods = listOf(Mood.AWESOME, Mood.GOOD, Mood.OKAY, Mood.BAD, Mood.TERRIBLE)
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { mood ->
                        Icon(
                            painter = painterResource(mood.iconRes),
                            tint = mood.color,
                            contentDescription = stringResource(mood.titleRes),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (entries.isNotEmpty())
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        val max = Mood.AWESOME.value
                        val count = entries.size

                        val offsets = entries.mapIndexed { index, entry ->
                            Offset(
                                w * ((if (index == 0) 0f else index + 1f) / count),
                                h * (1 - entry.mood.value.toFloat() / max.toFloat())
                            )
                        }
                        val path = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            offsets.forEachIndexed { index, offset ->
                                if (index == 0) return@forEachIndexed
                                quadTo(offsets[index - 1], offset)
                            }
                        }
                        val fillPath = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            offsets.forEachIndexed { index, offset ->
                                if (index == 0) return@forEachIndexed
                                quadTo(offsets[index - 1], offset)
                            }
                            lineTo(
                                if (offsets.size > 1)
                                    (offsets[offsets.size - 2].x + offsets.last().x) / 2
                                else offsets.last().x, h
                            )
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            fillPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    mostFrequentMood.color,
                                    Color.Transparent
                                ),
                                endY = h
                            )
                        )
                        drawPath(
                            path,
                            color = mostFrequentMood.color,
                            style = Stroke(8f, cap = StrokeCap.Round)
                        )
                    } else {
                    Text(
                        text = stringResource(Res.string.no_data_yet),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(
                text = if (monthly) stringResource(Res.string.mood_during_month)
                else stringResource(Res.string.mood_during_year),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

fun Path.quadTo(point1: Offset, point2: Offset) {
    quadraticTo(
        point1.x,
        point1.y,
        (point1.x + point2.x) / 2f,
        (point1.y + point2.y) / 2f
    )
}

@Preview
@Composable
fun MoodFlowChartPreview() {
    BasePreview {
        MoodFlowChart(
            entries = listOf(
                DiaryChartPoint(mood = Mood.AWESOME),
                DiaryChartPoint(mood = Mood.AWESOME),
                DiaryChartPoint(mood = Mood.GOOD),
                DiaryChartPoint(mood = Mood.OKAY),
                DiaryChartPoint(mood = Mood.GOOD),
                DiaryChartPoint(mood = Mood.BAD),
                DiaryChartPoint(mood = Mood.BAD),
                DiaryChartPoint(mood = Mood.TERRIBLE),
                DiaryChartPoint(mood = Mood.GOOD),
                DiaryChartPoint(mood = Mood.BAD),
            )
        )
    }
}

package com.mhss.app.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.DiaryChartPoint
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.domain.model.Mood
import com.mhss.app.ui.Res
import com.mhss.app.ui.mood_summary
import com.mhss.app.ui.most_of_the_time
import com.mhss.app.ui.no_data_yet
import com.mhss.app.ui.percent
import com.mhss.app.ui.your_mood_was
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoodCircularBar(
    modifier: Modifier = Modifier,
    entries: List<DiaryChartPoint>,
    strokeWidth: Float = 85f,
    showPercentage: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Column(
            Modifier.clickable {
                onClick()
            }
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
            val moods by remember(entries) {
                derivedStateOf {
                    entries.toPercentages()
                }
            }
            Text(
                text = stringResource(Res.string.mood_summary),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                textAlign = TextAlign.Center
            )
            if (entries.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    var currentAngle = remember { 90f }
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(29.dp)
                    ) {
                        for ((mood, percentage) in moods) {
                            drawArc(
                                color = mood.color,
                                startAngle = currentAngle,
                                sweepAngle = percentage * 360f,
                                useCenter = false,
                                size = Size(size.width, size.width),
                                style = Stroke(strokeWidth)
                            )
                            currentAngle += percentage * 360f
                        }
                    }
                    if (showPercentage) Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for ((mood, percentage) in moods) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(
                                        Res.string.percent,
                                        (percentage * 100).toInt()
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(mood.iconRes),
                                    contentDescription = stringResource(mood.titleRes),
                                    tint = mood.color,
                                    modifier = Modifier.size(strokeWidth.dp / 3)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.your_mood_was))
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = mostFrequentMood.color
                            )
                        ) {
                            append(stringResource(mostFrequentMood.titleRes))
                        }
                        append(stringResource(Res.string.most_of_the_time))
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(Res.string.no_data_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun List<DiaryChartPoint>.toPercentages(): Map<Mood, Float> {
    return this
        .sortedBy { it.mood.value }
        .groupingBy { it.mood }
        .eachCount()
        .mapValues { it.value / this.size.toFloat() }
}

@Preview
@Composable
fun MoodCircularBarPreview() {
    BasePreview {
        MoodCircularBar(
            entries = listOf(
                DiaryChartPoint(mood = Mood.AWESOME),
                DiaryChartPoint(mood = Mood.AWESOME),
                DiaryChartPoint(mood = Mood.GOOD),
                DiaryChartPoint(mood = Mood.OKAY),
                DiaryChartPoint(mood = Mood.OKAY),
                DiaryChartPoint(mood = Mood.BAD),
                DiaryChartPoint(mood = Mood.TERRIBLE),
                DiaryChartPoint(mood = Mood.TERRIBLE),
            )
        )
    }
}

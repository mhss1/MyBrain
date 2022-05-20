package com.mhss.app.mybrain.presentation.diary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.util.diary.Mood
import com.mhss.app.mybrain.R

@Composable
fun MoodFlowChart(
    entries: List<DiaryEntry>,
    monthly: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.mood_flow),
                style = MaterialTheme.typography.h5,
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
                val mostFrequentMood by derivedStateOf {
                    entries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key ?: Mood.OKAY
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
                            painter = painterResource(mood.icon),
                            tint = mood.color,
                            contentDescription = mood.name,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (entries.isNotEmpty())
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val w = constraints.maxWidth
                        val h = constraints.maxHeight

                        val max = Mood.AWESOME.value
                        val count = entries.size
                        val list = entries.mapIndexed { index, entry ->
                            Offset(
                                w * ((index.toFloat()) / count),
                                h * (1 - entry.mood.value.toFloat() / max.toFloat())
                            )
                        }
                        val path = Path().apply {
                            moveTo(list.first().x, list.first().y)
                            list.forEachIndexed { index, offset ->
                                if (index == 0) return@forEachIndexed
                                quadTo(list[index - 1], offset)
                            }
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            drawPath(
                                path,
                                color = mostFrequentMood.color,
                                style = Stroke(8f, cap = StrokeCap.Round)
                            )
                        }
                    } else {
                    Text(
                        text = stringResource(R.string.no_data_yet),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(
                text = if (monthly) stringResource(R.string.mood_during_month)
                else stringResource(R.string.mood_during_year),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

fun Path.quadTo(point1: Offset, point2: Offset) {
    quadraticBezierTo(
        point1.x,
        point1.y,
        (point1.x + point2.x) / 2f,
        (point1.y + point2.y) / 2f,
    )
}
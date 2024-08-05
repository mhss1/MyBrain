package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.Blue

@Composable
fun TasksSummaryCard(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
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
        val percentage by remember(tasks) {
            derivedStateOf {
                tasks.count { it.isCompleted }.toFloat() / tasks.size
            }
        }
        Column {
            Text(
                text = stringResource(R.string.tasks_summary),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                textAlign = TextAlign.Center
            )
            if (tasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(29.dp)
                    ) {
                        drawArc(
                            color = Color.Gray,
                            startAngle = 145f,
                            sweepAngle = 255f,
                            useCenter = false,
                            size = Size(size.width, size.width),
                            style = Stroke(65f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Blue,
                            startAngle = 145f,
                            sweepAngle = percentage * 255f,
                            useCenter = false,
                            size = Size(size.width, size.width),
                            style = Stroke(65f, cap = StrokeCap.Round)
                        )
                    }
                }
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.you_completed))
                        append(" ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(stringResource(R.string.percent, (percentage * 100).toInt()))
                        }
                        append(" ")
                        append(stringResource(R.string.of_last_week_tasks))
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.no_tasks_yet),
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
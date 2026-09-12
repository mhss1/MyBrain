package com.mhss.app.ui.components.tasks

import com.mhss.app.datetime.LocalDateTimeFormatter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.color
import com.mhss.app.datetime.isDueDateOverdue
import com.mhss.app.ui.Res
import com.mhss.app.ui.due_date
import com.mhss.app.ui.ic_alarm
import com.mhss.app.ui.ic_check
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LazyItemScope.TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val formatter = LocalDateTimeFormatter.current
    val formattedDate by remember(task.dueDate) {
        derivedStateOf { task.dueDate?.let { formatter.formatDateDependingOnDay(it) } ?: "" }
    }
    val isOverdue by remember(task.dueDate) {
        derivedStateOf { task.dueDate.isDueDateOverdue() }
    }
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .animateItem(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
    ) {
        Column(
            Modifier
                .clickable {
                    onClick()
                }
                .padding(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TaskCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.color,
                    onComplete = { onComplete() }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            if (task.subTasks.isNotEmpty() || task.dueDate != null) {
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (task.subTasks.isNotEmpty()) {
                        SubTasksProgressBar(subTasks = task.subTasks)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (task.dueDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(10.dp),
                                painter = painterResource(Res.drawable.ic_alarm),
                                contentDescription = stringResource(Res.string.due_date),
                                tint = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.8f
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.8f
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCheckBox(
    isComplete: Boolean,
    borderColor: Color,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable {
                onComplete()
            }, contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isComplete) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null
            )
        }
    }
}

@Composable
fun SubTasksProgressBar(modifier: Modifier = Modifier, subTasks: List<SubTask>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val completed = remember {
            subTasks.count { it.isCompleted }
        }
        val total = subTasks.size
        val progress by remember {
            derivedStateOf {
                completed.toFloat() / total.toFloat()
            }
        }
        val circleColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        val progressColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        Canvas(
            modifier = Modifier.size(12.dp)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.width / 2,
                style = Stroke(width = 7f)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                style = Stroke(width = 7f, cap = StrokeCap.Round),
                useCenter = false
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.bodySmall,
            color = progressColor,
        )
    }
}

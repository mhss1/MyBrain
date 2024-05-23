package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.tasks.SubTask
import com.mhss.app.mybrain.domain.model.tasks.Task
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import com.mhss.app.mybrain.util.date.isDueDateOverdue
import com.mhss.app.mybrain.util.settings.toPriority

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .animateItemPlacement(),
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Column(
            Modifier
                .clickable {
                    onClick()
                }
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TaskCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.toPriority().color,
                    onComplete = { onComplete() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (task.subTasks.isNotEmpty()) {
                    SubTasksProgressBar(
                        modifier = Modifier.padding(top = 8.dp),
                        subTasks = task.subTasks
                    )
                }
                Spacer(Modifier.width(8.dp))
                if (task.dueDate != 0L) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(13.dp),
                            painter = painterResource(R.drawable.ic_alarm),
                            contentDescription = stringResource(R.string.due_date),
                            tint = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colors.onBackground.copy(
                                alpha = 0.8f
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = task.dueDate.formatDateDependingOnDay(),
                            style = MaterialTheme.typography.body2,
                            color = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colors.onBackground.copy(
                                alpha = 0.8f
                            )
                        )
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
            .size(30.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .clickable {
                onComplete()
            }, contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isComplete) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_check),
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
        val circleColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)
        val progressColor = MaterialTheme.colors.onBackground.copy(alpha = 0.8f)
        Canvas(
            modifier = Modifier.size(16.dp)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.width / 2,
                style = Stroke(width = 8f)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                style = Stroke(width = 8f, cap = StrokeCap.Round),
                useCenter = false
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.body2,
            color = progressColor,
        )
    }
}

@Preview
@Composable
fun LazyItemScope.TaskItemPreview() {
    TaskItem(
        task = Task(
            title = "Task 1",
            description = "Task 1 description",
            dueDate = 1666999999999L,
            priority = 1,
            isCompleted = false
        ),
        onComplete = {},
        onClick = {}
    )
}
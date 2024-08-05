package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Priority
import com.mhss.app.ui.color
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mhss.app.util.date.isDueDateOverdue

@Composable
fun TaskDashboardItem(
    modifier: Modifier = Modifier,
    task: Task,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
    ) {
        Column(
            Modifier
                .clickable {
                    onClick()
                }
                .padding(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TaskDashboardCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.color,
                    onComplete = { onComplete() }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            if (task.dueDate != 0L) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(10.dp),
                        painter = painterResource(R.drawable.ic_alarm),
                        contentDescription = stringResource(R.string.due_date),
                        tint = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = task.dueDate.formatDateDependingOnDay(context),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (task.dueDate.isDueDateOverdue()) Color.Red else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDashboardCheckBox(
    isComplete: Boolean,
    borderColor: Color,
    onComplete: () -> Unit
) {
    Box(modifier = Modifier
        .size(22.dp)
        .clip(CircleShape)
        .border(2.dp, borderColor, CircleShape)
        .clickable {
            onComplete()
        }, contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isComplete) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun TaskDashboardItemPreview() {
    TaskDashboardItem(
        task = Task(
            title = "Task 1",
            description = "Task 1 description",
            dueDate = 1666999999999L,
            priority = Priority.MEDIUM,
            isCompleted = false
        ),
        onComplete = {},
        onClick = {}
    )
}
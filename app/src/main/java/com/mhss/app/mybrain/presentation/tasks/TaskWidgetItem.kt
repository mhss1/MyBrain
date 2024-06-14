package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.tasks.Priority
import com.mhss.app.mybrain.domain.model.tasks.Task
import com.mhss.app.mybrain.presentation.common.color
import com.mhss.app.mybrain.presentation.glance_widgets.tasks.CompleteTaskAction
import com.mhss.app.mybrain.presentation.glance_widgets.tasks.TaskWidgetItemClickAction
import com.mhss.app.mybrain.presentation.glance_widgets.tasks.completed
import com.mhss.app.mybrain.presentation.glance_widgets.tasks.taskId
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import com.mhss.app.mybrain.util.date.isDueDateOverdue

@Composable
fun TaskWidgetItem(
    task: Task
) {
    Box(
        GlanceModifier.padding(bottom = 3.dp)
    ) {
        Column(
            GlanceModifier
                .background(ImageProvider(R.drawable.small_item_rounded_corner_shape))
                .padding(10.dp)
                .clickable(
                    actionRunCallback<TaskWidgetItemClickAction>(
                        parameters = actionParametersOf(
                            taskId to task.id
                        )
                    )
                )
        ) {
            Row(GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionRunCallback<TaskWidgetItemClickAction>(
                    parameters = actionParametersOf(
                        taskId to task.id
                    )
                ))
                , verticalAlignment = Alignment.CenterVertically) {
                TaskWidgetCheckBox(
                    isComplete = task.isCompleted,
                    task.priority.color,
                    onComplete = actionRunCallback<CompleteTaskAction>(
                        parameters = actionParametersOf(
                            taskId to task.id,
                            completed to !task.isCompleted
                        )
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    task.title,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 2,
                    modifier = GlanceModifier.clickable(actionRunCallback<CompleteTaskAction>(
                        parameters = actionParametersOf(
                            taskId to task.id,
                            completed to !task.isCompleted
                        )
                    ))
                )

            }
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (task.subTasks.isNotEmpty()){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.padding(top = 4.dp)
                    ) {
                        val completed = remember {
                            task.subTasks.count { it.isCompleted }
                        }
                        val total = task.subTasks.size
                        Image(
                            provider = ImageProvider(R.drawable.ic_bullet_list),
                            modifier = GlanceModifier
                                .size(12.dp),
                            contentDescription = null
                        )
                        Spacer(GlanceModifier.width(3.dp))
                        Text(
                            text = "$completed/$total",
                            style = TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                                fontSize = 12.sp,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                    }
                    Spacer(GlanceModifier.width(4.dp))
                }
                if (task.dueDate != 0L) {
                    Row(
                        modifier = GlanceModifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = GlanceModifier.size(12.dp),
                            provider = if (task.dueDate.isDueDateOverdue()) ImageProvider(R.drawable.ic_alarm_red) else ImageProvider(
                                R.drawable.ic_alarm
                            ),
                            contentDescription = "",
                        )
                        Spacer(GlanceModifier.width(3.dp))
                        Text(
                            text = task.dueDate.formatDateDependingOnDay(),
                            style = TextStyle(
                                color = if (task.dueDate.isDueDateOverdue()) ColorProvider(Color.Red) else ColorProvider(
                                    Color.White.copy(0.8f)
                                ),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskWidgetCheckBox(
    isComplete: Boolean,
    borderColor: Color,
    onComplete: Action
) {
    Box(
        modifier = GlanceModifier
            .size(25.dp)
            .cornerRadius(99.dp)
            .background(ImageProvider(
                when (borderColor){
                      Priority.LOW.color -> R.drawable.task_check_box_background_green
                      Priority.MEDIUM.color -> R.drawable.task_check_box_background_orange
                    else -> R.drawable.task_check_box_background_red
                }))
            .clickable(
                onClick = onComplete
            ).padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isComplete) {
            Image(
                modifier = GlanceModifier.size(14.dp),
                provider = ImageProvider(R.drawable.ic_check),
                contentDescription = null
            )
        }
    }
}
package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.presentation.glance_widgets.AddTaskAction
import com.mhss.app.mybrain.presentation.glance_widgets.NavigateToTasksAction

@Composable
fun TasksHomeScreenWidget(
    tasks: List<Task>
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(R.drawable.large_item_rounded_corner_shape))
            .cornerRadius(25.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .clickable(onClick = actionRunCallback<NavigateToTasksAction>())
                .padding(8.dp)
        ) {
            Row(
                GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    getString(R.string.tasks),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier.padding(horizontal = 8.dp),
                )
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        text = "",
                        modifier = GlanceModifier
                            .size(22.dp)
                            .background(ImageProvider(R.drawable.ic_add))
                            .padding(8.dp)
                        ,
                        onClick = actionRunCallback<AddTaskAction>()
                    )
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            LazyColumn(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
                    .background(ImageProvider(R.drawable.large_inner_item_rounded_corner_shape))
                    .cornerRadius(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    if (tasks.isEmpty()) {
                        item {
                            Text(
                                text = getString(R.string.no_tasks_message),
                                modifier = GlanceModifier.padding(16.dp),
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        item { Spacer(GlanceModifier.height(6.dp)) }
                        items(tasks) { task ->
                            TaskWidgetItem(task)
                        }
                    }
            }
        }
    }
}
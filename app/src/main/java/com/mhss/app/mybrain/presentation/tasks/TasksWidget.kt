package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.ui.theme.LightGray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksWidget(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit = {},
    onCheck: (Task) -> Unit = {},
    onAddClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val isDark = !MaterialTheme.colors.isLight
        Column(
            modifier = modifier
                .clickable { onClick() }
                .padding(8.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.tasks), style = MaterialTheme.typography.body1)
                Icon(
                    painterResource(R.drawable.ic_add),
                    stringResource(R.string.add_event),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onAddClick()
                        }
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color.DarkGray else LightGray),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (tasks.isEmpty()){
                    item {
                        Text(
                            text = stringResource(R.string.no_tasks_today),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else items(tasks) {
                    TaskWidgetItem(
                        task = it,
                        onClick = { onTaskClick(it) },
                        onComplete = { onCheck(it.copy(isCompleted = !it.isCompleted)) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}
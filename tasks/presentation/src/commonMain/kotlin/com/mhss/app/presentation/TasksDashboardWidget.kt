package com.mhss.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_event
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.no_tasks_message
import com.mhss.app.ui.tasks
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksDashboardWidget(
    modifier: Modifier = Modifier,
    tasks: LazyPagingItems<Task>,
    onTaskClick: (Task) -> Unit = {},
    onCheck: (Task, Boolean) -> Unit = {_,_ ->},
    onAddClick: () -> Unit = {},
    onClick: () -> Unit = {}
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
                Text(stringResource(Res.string.tasks), style = MaterialTheme.typography.bodyLarge)
                Icon(
                    painterResource(Res.drawable.ic_add),
                    stringResource(Res.string.add_event),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onAddClick()
                        }
                )
            }
            Spacer(Modifier.height(8.dp))
            PagingLoadState(tasks)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.07f).compositeOver(
                        MaterialTheme.colorScheme.surfaceVariant)
                    ),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (tasks.itemCount == 0 && tasks.loadState.refresh is LoadState.NotLoading){
                    item {
                        Text(
                            text = stringResource(Res.string.no_tasks_message),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else items(tasks.itemCount, key = tasks.itemKey { it.id }) { index ->
                    tasks[index]?.let { task ->
                        TaskSmallCard(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onComplete = { onCheck(task, !task.isCompleted) },
                            modifier = Modifier.animateItem()
                        )
                    } ?: PagingPlaceholder()
                }
            }
        }
    }
}

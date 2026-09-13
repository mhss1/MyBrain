package com.mhss.app.ui.components.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.search_tasks
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskSearchContent(
    tasks: LazyPagingItems<Task>,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onTaskClick: (Task) -> Unit,
    onCompleteTask: (Task) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        var query by rememberSaveable {
            mutableStateOf("")
        }
        LaunchedEffect(query) { onQueryChange(query) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(true) { focusRequester.requestFocus() }
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            label = { Text(stringResource(Res.string.search_tasks)) },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester)
        )
        PagingLoadState(tasks)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(tasks.itemCount, key = tasks.itemKey { it.id }) { index ->
                tasks[index]?.let { task ->
                    TaskCard(
                        task = task,
                        onComplete = {
                            onCompleteTask(task)
                        },
                        onClick = {
                            onTaskClick(task)
                        },
                    )
                } ?: PagingPlaceholder(Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask

@Composable
fun SubTaskItem(
    subTask: SubTask,
    onChange: (SubTask) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = stringResource(R.string.delete_sub_task),
            modifier = Modifier.clickable { onDelete() }
        )
        Checkbox(
            checked = subTask.isCompleted,
            onCheckedChange = { onChange(subTask.copy(isCompleted = it)) },
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = subTask.title,
            onValueChange = {
                onChange(subTask.copy(title = it))
            },
            textStyle = if (subTask.isCompleted)
                TextStyle(
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colors.onBackground
                )
            else
                MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onBackground),
            modifier = Modifier.fillMaxWidth()
        )

    }
}

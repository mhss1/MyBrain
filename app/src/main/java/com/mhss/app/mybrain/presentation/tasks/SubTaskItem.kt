package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubTaskItem(
    subTask: SubTask,
    onChange: (SubTask) -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = stringResource(R.string.delete_sub_task),
            modifier = Modifier.clickable { onDelete() }
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
                checked = subTask.isCompleted,
                onCheckedChange = { onChange(subTask.copy(isCompleted = it)) },
            )
        }
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = subTask.title,
            onValueChange = {
                onChange(subTask.copy(title = it))
            },
            textStyle =
            MaterialTheme.typography.body1.copy(
                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                color = MaterialTheme.colors.onBackground
            ),
            modifier = Modifier
                .padding(top = 4.dp)
                .weight(1f)
                .background(
                    MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    interactionSource,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                ),
            cursorBrush = SolidColor(MaterialTheme.colors.primary)
        ) {
            TextFieldDefaults.TextFieldDecorationBox(
                contentPadding = PaddingValues(8.dp),
                visualTransformation = VisualTransformation.None,
                enabled = true,
                singleLine = false,
                value = subTask.title,
                interactionSource = interactionSource,
                innerTextField = it
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun SubTaskItemPreview() {
    SubTaskItem(subTask = SubTask("Title", true), {}, {})
}

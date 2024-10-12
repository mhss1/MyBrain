package com.mhss.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.presentation.LeftToRight
import com.mhss.app.ui.gradientBrushColor
import com.mhss.app.ui.theme.MyBrainTheme

@Composable
fun AiChatBar(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    loading: Boolean,
    attachments: List<AiMessageAttachment>,
    onTextChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    onRemoveAttachment: (Int) -> Unit,
    onSend: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(attachments.isNotEmpty()) {
            AiAttachmentsSection(
                attachments = attachments,
                editable = true,
                onRemove = onRemoveAttachment
            )
        }
        LeftToRight {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    onValueChange = onTextChange,
                    shape = RoundedCornerShape(99.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 6.dp, bottom = 8.dp, start = 8.dp)
                        .heightIn(0.dp, 400.dp)
                        .border(1.5.dp, gradientBrushColor(), RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = { onAttachClick() }) {
                            Icon(
                                painterResource(id = R.drawable.ic_attach),
                                contentDescription = null
                            )
                        }
                    },
                )
                if (loading) {
                    CircularProgressIndicator(
                        Modifier
                            .padding(8.dp)
                            .size(32.dp)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        gradientBrushColor(),
                                        blendMode = BlendMode.SrcAtop
                                    )
                                }
                            }
                    )
                } else {
                    IconButton(
                        onClick = { onSend() },
                        enabled = enabled,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "Send",
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        if (enabled) {
                                            drawRect(
                                                gradientBrushColor(),
                                                blendMode = BlendMode.SrcAtop
                                            )
                                        } else {
                                            drawRect(
                                                Color.Gray,
                                                blendMode = BlendMode.SrcAtop
                                            )
                                        }
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AiChatBarPreview() {
    MyBrainTheme {
        AiChatBar(
            text = "Hello, World!",
            enabled = true,
            attachments = listOf(
                AiMessageAttachment.Note(
                    Note(
                        id = 1,
                        title = "This is a Note Title",
                        content = "Note Content",
                    )
                ),
                AiMessageAttachment.Task(
                    Task(
                        id = 1,
                        title = "This is a Task Title",
                        description = "Task Description",
                        isCompleted = false,
                        dueDate = 12345,
                        subTasks = listOf(
                            SubTask()
                        )
                    )
                )
            ),
            loading = false,
            onTextChange = {},
            onAttachClick = {},
            onRemoveAttachment = {},
            onSend = {},
        )
    }
}
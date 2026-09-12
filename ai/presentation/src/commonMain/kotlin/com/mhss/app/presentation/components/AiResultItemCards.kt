package com.mhss.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.Res
import com.mhss.app.ui.all_day
import com.mhss.app.ui.color
import com.mhss.app.ui.components.common.previewMarkdownTypography
import com.mhss.app.ui.components.tasks.SubTasksProgressBar
import com.mhss.app.ui.due_date
import com.mhss.app.ui.event_time
import com.mhss.app.ui.event_time_at
import com.mhss.app.ui.ic_alarm
import com.mhss.app.ui.ic_check
import com.mhss.app.ui.preview.BasePreview
import com.mikepenz.markdown.m3.Markdown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun AiNoteCard(
    note: Note,
    onClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalDateTimeFormatter.current
    val formattedDate by remember(note.updatedDate) {
        derivedStateOf { formatter.formatDateDependingOnDay(note.updatedDate) }
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        onClick = { onClick(note) }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                note.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            val truncatedContent = if (note.content.length > 60) {
                note.content.take(60) + "..."
            } else {
                note.content
            }
            Markdown(
                content = truncatedContent,
                typography = previewMarkdownTypography(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun AiTaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalDateTimeFormatter.current
    val formattedDate by remember(task.dueDate) {
        derivedStateOf {
            task.dueDate?.let { formatter.formatDateDependingOnDay(it) } ?: ""
        }
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, task.priority.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.subTasks.isNotEmpty() || task.dueDate != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (task.subTasks.isNotEmpty()) {
                            SubTasksProgressBar(subTasks = task.subTasks)
                        }
                        Spacer(Modifier.width(6.dp))
                        if (task.dueDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    modifier = Modifier.size(10.dp),
                                    painter = painterResource(Res.drawable.ic_alarm),
                                    contentDescription = stringResource(Res.string.due_date),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiCalendarEventCard(
    event: CalendarEvent,
    onClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalDateTimeFormatter.current
    val allDayString = stringResource(Res.string.all_day)
    val eventTimeAt = stringResource(Res.string.event_time_at)
    val eventTime = stringResource(Res.string.event_time)
    val formattedDateTime by remember(event.start, event.end, event.location, event.allDay) {
        derivedStateOf {
            formatter.formatEventStartEnd(
                start = event.start,
                end = event.end,
                allDayString = allDayString,
                eventTimeAt = eventTimeAt,
                eventTime = eventTime,
                location = event.location,
                allDay = event.allDay,
            )
        }
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        onClick = { onClick(event) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(event.color)),
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formattedDateTime,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiNoteCardPreview() {
    BasePreview {
        AiNoteCard(
            note = Note(
                title = "Sample Note",
                content = "This is a sample note content for previewing.",
                updatedDate = System.currentTimeMillis()
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview(showBackground = true)
@Composable
private fun AiTaskCardPreview() {
    BasePreview {
        AiTaskCard(
            task = Task(
                id = "1",
                title = "Sample Task",
                priority = Priority.HIGH,
                dueDate = System.currentTimeMillis(),
                subTasks = listOf(
                    SubTask(
                        title = "Test SubTask 1",
                        isCompleted = false
                    ),
                    SubTask(
                        title = "Test SubTask 2",
                        isCompleted = true
                    ),
                    SubTask(
                        title = "Test SubTask 3",
                        isCompleted = false
                    )
                )
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AiCalendarEventCardPreview() {
    BasePreview {
        AiCalendarEventCard(
            event = CalendarEvent(
                id = 1L,
                title = "Sample Event",
                start = System.currentTimeMillis(),
                end = System.currentTimeMillis() + 3600000,
                calendarId = 1L,
                color = Color.Red.toArgb()
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

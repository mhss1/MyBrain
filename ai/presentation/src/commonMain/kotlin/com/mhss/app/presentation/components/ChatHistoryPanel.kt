package com.mhss.app.presentation.components

import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.components.isEmpty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.ui.Res
import com.mhss.app.ui.cancel
import com.mhss.app.ui.chat_history
import com.mhss.app.ui.delete
import com.mhss.app.ui.delete_all
import com.mhss.app.ui.delete_all_history_title
import com.mhss.app.ui.delete_all_threads_confirmation
import com.mhss.app.ui.delete_chat
import com.mhss.app.ui.delete_chat_title
import com.mhss.app.ui.delete_thread_confirmation
import com.mhss.app.ui.ic_delete
import com.mhss.app.ui.new_chat
import com.mhss.app.ui.no_chats
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatHistoryPanel(
    threads: LazyPagingItems<AssistantThread>,
    currentThreadId: String?,
    onDismiss: () -> Unit,
    onThreadSelected: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onDeleteAllThreads: () -> Unit,
) {
    var threadToDelete by remember { mutableStateOf<AssistantThread?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.chat_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (threads.itemCount > 0) {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = stringResource(Res.string.delete_all),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        PagingLoadState(threads)

        if (threads.isEmpty) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_chats),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(threads.itemCount, key = threads.itemKey { it.id }) { index ->
                    threads[index]?.let { thread ->
                        val isSelected = thread.id == currentThreadId
                        val formatter = LocalDateTimeFormatter.current
                        val dateStr = formatter.formatDateDependingOnDay(thread.updatedAt)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            onClick = {
                                onThreadSelected(thread.id)
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = thread.title.ifBlank { stringResource(Res.string.new_chat) },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { threadToDelete = thread },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_delete),
                                        contentDescription = stringResource(Res.string.delete_chat),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } ?: PagingPlaceholder(Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }
        }
    }

    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = { Text(stringResource(Res.string.delete_chat_title)) },
            text = { Text(stringResource(Res.string.delete_thread_confirmation)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        onDeleteThread(thread.id)
                        threadToDelete = null
                    }
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                Button(
                    shape = RoundedCornerShape(25.dp),
                    onClick = { threadToDelete = null },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(Res.string.delete_all_history_title)) },
            text = { Text(stringResource(Res.string.delete_all_threads_confirmation)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        onDeleteAllThreads()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(stringResource(Res.string.delete_all))
                }
            },
            dismissButton = {
                Button(
                    shape = RoundedCornerShape(25.dp),
                    onClick = { showDeleteAllDialog = false },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

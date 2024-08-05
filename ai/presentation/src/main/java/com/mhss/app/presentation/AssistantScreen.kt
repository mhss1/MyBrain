package com.mhss.app.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.presentation.components.AiChatBar
import com.mhss.app.presentation.components.MessageCard
import com.mhss.app.ui.toUserMessage
import com.mhss.app.util.date.now
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val messages = uiState.messages
    val loading = uiState.loading
    val error = uiState.error
    var text by rememberSaveable { mutableStateOf("") }
    val attachments = remember {
        mutableStateListOf<AiMessageAttachment>()
    }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(loading) {
        lazyListState.animateScrollToItem(0)
        if (loading) {
            // TODO: start loading animation
        } else {
            // TODO: stop loading animation
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.assistant),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AiChatBar(
                text = text,
                enabled = viewModel.aiEnabled && !loading && text.isNotBlank(),
                attachments = attachments,
                onTextChange = { text = it },
                onAttachClick = {
                    // TODO
                },
                onRemoveAttachment = { attachments.removeAt(it) },
                onSend = {
                    viewModel.onEvent(
                        AssistantEvent.SendMessage(
                            AiMessage(
                                text,
                                AiMessageType.USER,
                                now(),
                                attachments.toList() // a copy
                            )
                        )
                    )
                    text = ""
                    attachments.clear()
                    keyboardController?.hide()
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (viewModel.aiEnabled) {
                LeftToRight {
                    LazyColumn(
                        state = lazyListState,
                        reverseLayout = true
                    ) {
                        item { Spacer(Modifier.height(20.dp)) }
                        error?.let { error ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    colors = CardDefaults.cardColors(
                                        contentColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = error.toUserMessage(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.CenterHorizontally),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        items(messages, key = { it.time }) { message ->
                            MessageCard(
                                message = message,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("label", message.content)
                                    clipboard.setPrimaryClip(clip)
                                }
                            )
                        }
                    }
                }
            } else {
                // TODO: Show AI disabled message
            }
        }

    }
}

@Composable
fun LeftToRight(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content)
}
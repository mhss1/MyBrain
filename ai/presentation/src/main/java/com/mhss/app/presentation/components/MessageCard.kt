package com.mhss.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.R
import com.mhss.app.ui.gradientBrushColor
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.SecondaryColor
import com.mhss.app.util.date.formatTime
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography


@Composable
fun LazyItemScope.MessageCard(
    message: AiMessage,
    onCopy: () -> Unit,
) {
    val isUser = remember {
        message.type == AiMessageType.USER
    }
    var showContextMenu by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                end = if (isUser) 8.dp else 48.dp,
                start = if (isUser) 48.dp else 8.dp,
                bottom = 4.dp,
                top = 8.dp
            )
            .animateItem(
                fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            )
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = if (isUser) 20.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 20.dp,
                bottomStart = if (isUser) 24.dp else 14.dp,
                bottomEnd = if (isUser) 14.dp else 20.dp
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = { showContextMenu = true }
        ) {
            val context = LocalContext.current
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            Column(
                modifier = Modifier
                    .drawBehind {
                        if (isUser) drawRect(
                            gradientBrushColor(
                                0f to SecondaryColor,
                                1f to surfaceVariant
                                    .copy(alpha = 0.3f)
                                    .compositeOver(
                                        SecondaryColor
                                    )
                            ), Offset.Zero, size,
                            size.minDimension * 1.5f
                        )
                        else {
                            drawAiGradientRadials(
                                surfaceVariant,
                                radius = size.minDimension * 1.2f
                            )
                        }
                    }
            ) {
                Markdown(
                    content = message.content,
                    modifier =  Modifier.padding(
                        top = 7.dp,
                        start = if (isUser) 12.dp else 8.dp,
                        end = if (isUser) 8.dp else 12.dp,
                    ),
                    imageTransformer = Coil2ImageTransformerImpl,
                    colors = markdownColor(
                        text = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        linkText = Color.Blue
                    ),
                    typography = markdownTypography(
                        text = MaterialTheme.typography.bodyMedium,
                        h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                )
                if (message.attachments.isNotEmpty()) {
                    AiAttachmentsSection(
                        attachments = message.attachments,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Text(
                    text = message.time.formatTime(context),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUser) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(id = R.string.copy))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = stringResource(id = R.string.copy)
                            )
                        }
                        ,onClick = {
                            showContextMenu = false
                            onCopy()
                        }
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview
@Composable
fun MessageCardPreview() {
    MyBrainTheme {
        val demoText = remember {
            LoremIpsum(60).values.first()
        }
        LazyColumn {
            item {
                MessageCard(
                    message = AiMessage(
                        demoText,
                        AiMessageType.USER,
                        1111111111,
                        listOf(
                            AiMessageAttachment.Note(
                                Note(
                                    "This is a test tile for the note",
                                    "Description",
                                    1111111111
                                )
                            ),
                            AiMessageAttachment.CalenderEvents,

                        )
                    )
                ){}
            }
            item {
                MessageCard(
                    message = AiMessage(
                        demoText,
                        AiMessageType.MODEL,
                        1111111111
                    )
                ){}
            }
        }
    }
}
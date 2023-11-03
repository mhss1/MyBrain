package com.mhss.app.mybrain.presentation.diary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.util.date.fullDate
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.DiaryEntryItem(
    modifier: Modifier = Modifier,
    entry: DiaryEntry,
    onClick: (DiaryEntry) -> Unit
) {
    Card(
        modifier = modifier
            .animateItemPlacement(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(entry) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(entry.mood.icon),
                    stringResource(entry.mood.title),
                    tint = entry.mood.color,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    entry.title,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.content.isNotBlank()){
                MarkdownText(
                    markdown = entry.content,
                    maxLines = 10,
                    onClick = {onClick(entry)},
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = entry.createdDate.fullDate(),
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
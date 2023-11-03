package com.mhss.app.mybrain.presentation.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.ui.theme.Orange
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun NoteItem(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: (Note) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(note) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                if (note.pinned){
                    Icon(
                        painter = painterResource(R.drawable.ic_pin_filled),
                        contentDescription = stringResource(R.string.pin_note),
                        tint = Orange,
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    note.title,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            MarkdownText(
                markdown = note.content,
                maxLines = 14,
                onClick = {onClick(note)},
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.updatedDate.formatDateDependingOnDay(),
                style = MaterialTheme.typography.caption.copy(color = Color.Gray),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
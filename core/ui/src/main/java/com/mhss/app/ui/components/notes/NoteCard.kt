package com.mhss.app.ui.components.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.Orange
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: (Note) -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
        onClick = { onClick(note) }
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(note) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.pinned) {
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Markdown(
                content = note.content,
                colors = markdownColor(
                    linkText = Color.Blue
                ),
                typography = markdownTypography(
                    text = MaterialTheme.typography.bodyMedium,
                    h1 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    h2 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    h3 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    h4 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    h5 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    h6 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    code = MaterialTheme.typography.bodySmall,
                    paragraph = MaterialTheme.typography.bodyMedium,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.updatedDate.formatDateDependingOnDay(context),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
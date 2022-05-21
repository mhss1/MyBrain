package com.mhss.app.mybrain.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.util.date.formatEventStartEnd

@Composable
fun CalendarEventWidgetItem(
    event: CalendarEvent,
    onClick: (CalendarEvent) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 6.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(event.color)),
            )
            Spacer(Modifier.width(4.dp))
            Column(
                modifier = Modifier
                    .clickable { onClick(event) }
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    formatEventStartEnd(
                        start = event.start,
                        end = event.end,
                        location = event.location,
                        allDay = event.allDay,
                    ),
                    style = MaterialTheme.typography.body2,
                )

            }
        }
    }
}

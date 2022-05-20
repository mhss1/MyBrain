package com.mhss.app.mybrain.presentation.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.util.date.formatEventStartEnd

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.CalendarEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
    onClick: (CalendarEvent) -> Unit
) {
    Card(
        modifier = modifier
            .animateItemPlacement(),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        border = BorderStroke(2.dp, Color(event.color))
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(event) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                formatEventStartEnd(
                    start = event.start,
                    end = event.end,
                    location = event.location,
                    allDay = event.allDay,
                ),
                style = MaterialTheme.typography.body1,
            )

        }
    }
}

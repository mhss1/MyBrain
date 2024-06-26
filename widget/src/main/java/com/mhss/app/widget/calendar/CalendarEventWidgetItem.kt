package com.mhss.app.widget.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.util.date.formatEventStartEnd
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun CalendarEventWidgetItem(
    event: CalendarEvent,
) {
    val context = LocalContext.current
    Box(
        GlanceModifier
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(start = 8.dp)
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(6.dp)
                        .height(26.dp)
                        .cornerRadius(6.dp)
                        .background(Color(event.color)),
                ) {}
                Spacer(GlanceModifier.width(4.dp))
                Column(
                    modifier = GlanceModifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        event.title,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 2
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        context.formatEventStartEnd(
                            start = event.start,
                            end = event.end,
                            location = event.location,
                            allDay = event.allDay,
                        ),
                        style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer)
                    )
                }
            }
            Box(GlanceModifier.fillMaxSize().clickable(
                actionRunCallback<CalendarWidgetItemClick>(
                    parameters = actionParametersOf(
                        eventJson to Json.encodeToString(event)
                    )
                )
            )) {}
        }
    }
}
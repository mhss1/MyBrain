package com.mhss.app.mybrain.presentation.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.ui.theme.LightGray

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CalendarWidget(
    modifier: Modifier = Modifier,
    events: Map<String, List<CalendarEvent>>,
    onPermission: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val context = LocalContext.current
        val readCalendarPermissionState = rememberPermissionState(
            Manifest.permission.READ_CALENDAR
        )
        val isDark = !MaterialTheme.colors.isLight
        Column(
            modifier = modifier
                .clickable { onClick() }
                .padding(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.calendar), style = MaterialTheme.typography.body1)
                Icon(
                    painterResource(R.drawable.ic_add),
                    stringResource(R.string.add_event),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_INSERT)
                            intent.type = "vnd.android.cursor.item/event"
                            context.startActivity(intent)
                        }
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color.DarkGray else LightGray),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (readCalendarPermissionState.hasPermission) {
                    if (events.isEmpty()) {
                        item {
                            LaunchedEffect(true) { onPermission(true) }
                            Text(
                                text = stringResource(R.string.no_events),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    } else {
                        events.forEach { (day, events) ->
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.body2
                                    )
                                    events.forEach { event ->
                                        CalendarEventWidgetItem(event = event, onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.data = ContentUris.withAppendedId(
                                                CalendarContract.Events.CONTENT_URI,
                                                event.id
                                            )
                                            context.startActivity(intent)
                                        })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        LaunchedEffect(true) { onPermission(false) }
                        NoReadCalendarPermissionMessage(
                            shouldShowRationale = readCalendarPermissionState.shouldShowRationale,
                            context
                        ) {
                            readCalendarPermissionState.launchPermissionRequest()
                        }
                    }
                }
            }
        }
    }
}
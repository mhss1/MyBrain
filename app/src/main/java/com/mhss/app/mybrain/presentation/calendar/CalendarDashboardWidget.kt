package com.mhss.app.mybrain.presentation.calendar

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent
import com.mhss.app.mybrain.ui.theme.LightGray

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CalendarDashboardWidget(
    modifier: Modifier = Modifier,
    events: Map<String, List<CalendarEvent>>,
    onPermission: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    onAddEventClicked: () -> Unit = {},
    onEventClicked: (CalendarEvent) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val context = LocalContext.current
        val readCalendarPermissionState = rememberPermissionState(
            Manifest.permission.READ_CALENDAR
        )
        // Workaround replacement to Material2 `isLight`
        val isDark = MaterialTheme.colorScheme.background.luminance() <= 0.5
        Column(
            modifier = modifier
                .clickable { onClick() }
                .padding(8.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.calendar), style = MaterialTheme.typography.bodyLarge)
                Icon(
                    painterResource(R.drawable.ic_add),
                    stringResource(R.string.add_event),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onAddEventClicked()
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
                if (readCalendarPermissionState.status.isGranted) {
                    if (events.isEmpty()) {
                        item {
                            LaunchedEffect(true) { onPermission(true) }
                            Text(
                                text = stringResource(R.string.no_events),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        events.forEach { (day, events) ->
                            item {
                                LaunchedEffect(true) { onPermission(true) }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    events.forEach { event ->
                                        CalendarEventSmallItem(event = event, onClick = {
                                            onEventClicked(event)
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
                            shouldShowRationale = readCalendarPermissionState.status.shouldShowRationale,
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
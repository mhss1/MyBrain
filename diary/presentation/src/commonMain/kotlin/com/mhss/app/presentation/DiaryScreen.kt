@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_entry
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.diary
import com.mhss.app.ui.diary_chart
import com.mhss.app.ui.diary_img
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_chart
import com.mhss.app.ui.ic_search
import com.mhss.app.ui.ic_settings_sliders
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_entries_message
import com.mhss.app.ui.order_by
import com.mhss.app.ui.search
import com.mhss.app.ui.titleRes
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    navController: NavHostController,
    viewModel: DiaryViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val liquidState = rememberLiquidState()
    Scaffold(
        topBar = {
            MyBrainAppBar(
                title = stringResource(Res.string.diary),
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.DiaryChartScreen)
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_chart),
                            contentDescription = stringResource(Res.string.diary_chart),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LiquidFloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.DiaryDetailScreen()
                    )
                },
                iconPainter = painterResource(Res.drawable.ic_add),
                contentDescription = stringResource(Res.string.add_entry),
                liquidState = liquidState
            )
        }
    ) { paddingValues ->
        if (uiState.entries.isEmpty()) {
            NoEntriesMessage()
        }
        Column(Modifier.padding(paddingValues).liquefiable(liquidState)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { orderSettingsVisible = !orderSettingsVisible }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(Res.drawable.ic_settings_sliders),
                        contentDescription = stringResource(Res.string.order_by)
                    )
                }
                IconButton(onClick = {
                    navController.navigate(Screen.DiarySearchScreen)
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = stringResource(Res.string.search)
                    )
                }
            }
            AnimatedVisibility(visible = orderSettingsVisible) {
                DiarySettingsSection(
                    uiState.entriesSortOrder,
                    onOrderChange = {
                        viewModel.onEvent(DiaryEvent.UpdateOrder(it))
                    },
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                uiState.entries.forEach { (day, entries) ->
                    stickyHeader {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(bottom = 4.dp)
                                .padding(horizontal = 12.dp)
                        )
                    }
                    items(entries) { entry ->
                        DiaryEntryItem(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            entry = entry,
                            timeText = LocalDateTimeFormatter.current.formatTime(entry.createdDate),
                            onClick = {
                                navController.navigate(
                                    Screen.DiaryDetailScreen(
                                        entry.id
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiarySettingsSection(sortOrder: SortOrder, onOrderChange: (SortOrder) -> Unit) {
    val sortOrders = remember {
        listOf(
            SortOrder.DateModified(),
            SortOrder.DateCreated(),
            SortOrder.Alphabetical()
        )
    }
    val sortTypes = remember {
        listOf(
            SortType.ASC,
            SortType.DESC
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(Res.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            sortOrders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder::class == it::class,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    it.copyOrder(sortType = sortOrder.sortType)
                                )
                        }
                    )
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            sortTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder.sortType == it,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    sortOrder.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun NoEntriesMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_entries_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(Res.drawable.diary_img),
            contentDescription = stringResource(Res.string.no_entries_message),
            alpha = 0.7f
        )
    }
}

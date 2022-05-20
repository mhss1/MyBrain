package com.mhss.app.mybrain.presentation.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.presentation.tasks.AnimatedTabIndicator

@Composable
fun DiaryChartScreen(
    viewModel: DiaryViewModel = hiltViewModel()
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val state = viewModel.uiState
        var monthly by remember { mutableStateOf(true) }
        MonthlyOrYearlyTab {
            viewModel.onEvent(DiaryEvent.ChangeChartEntriesRange(it))
            monthly = it
        }
        MoodCircularBar(entries = state.chartEntries)
        MoodFlowChart(entries = state.chartEntries, monthly)
    }
}

@Composable
fun MonthlyOrYearlyTab(
    onChange: (Boolean) -> Unit
) {
    var selected by remember {
        mutableStateOf(getString(R.string.last_30_days))
    }
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        AnimatedTabIndicator(Modifier.tabIndicatorOffset(tabPositions[if (selected == stringResource(R.string.last_30_days)) 0 else 1]))
    }
    LaunchedEffect(true){
        onChange(true)
    }
    TabRow(
        selectedTabIndex = if (selected == stringResource(R.string.last_30_days)) 0 else 1,
        indicator = indicator,
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        Tab(
            text = { Text(stringResource(R.string.last_30_days)) },
            selected = selected == stringResource(R.string.last_30_days),
            onClick = {
                selected = getString(R.string.last_30_days)
                onChange(true)
            },
        )
        Tab(
            text = { Text(stringResource(R.string.last_year)) },
            selected = selected == stringResource(R.string.last_year),
            onClick = {
                selected = getString(R.string.last_year)
                onChange(false)
            }
        )
    }
}
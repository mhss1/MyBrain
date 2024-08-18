package com.mhss.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.AnimatedTabIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun DiaryChartScreen(
    viewModel: DiaryViewModel = koinViewModel()
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .padding(WindowInsets.systemBars.asPaddingValues()),
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
        mutableIntStateOf(R.string.last_30_days)
    }
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        AnimatedTabIndicator(Modifier.tabIndicatorOffset(tabPositions[if (selected == R.string.last_30_days) 0 else 1]))
    }
    LaunchedEffect(true){
        onChange(true)
    }
    TabRow(
        selectedTabIndex = if (selected == R.string.last_30_days) 0 else 1,
        indicator = indicator,
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        Tab(
            text = { Text(stringResource(R.string.last_30_days)) },
            selected = selected == R.string.last_30_days,
            onClick = {
                selected = R.string.last_30_days
                onChange(true)
            },
        )
        Tab(
            text = { Text(stringResource(R.string.last_year)) },
            selected = selected == R.string.last_year,
            onClick = {
                selected = R.string.last_year
                onChange(false)
            }
        )
    }
}
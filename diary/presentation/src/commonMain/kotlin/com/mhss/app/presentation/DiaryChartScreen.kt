package com.mhss.app.presentation

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.common.AnimatedTabIndicator
import com.mhss.app.ui.last_30_days
import com.mhss.app.ui.last_year
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        val entries by viewModel.chartEntries.collectAsStateWithLifecycle()
        var monthly by remember { mutableStateOf(true) }
        MonthlyOrYearlyTab {
            viewModel.onEvent(DiaryEvent.ChangeChartEntriesRange(it))
            monthly = it
        }
        MoodCircularBar(entries = entries)
        MoodFlowChart(entries = entries, monthly)
    }
}

@Composable
fun MonthlyOrYearlyTab(
    onChange: (Boolean) -> Unit
) {
    var monthlySelected by remember { mutableStateOf(true) }
    LaunchedEffect(true){
        onChange(true)
    }
    PrimaryTabRow(
        selectedTabIndex = if (monthlySelected) 0 else 1,
        indicator = {
            AnimatedTabIndicator(Modifier.tabIndicatorOffset(if (monthlySelected) 0 else 1))
        },
        divider = {},
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        Tab(
            text = { Text(
                stringResource(Res.string.last_30_days),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) },
            selected = monthlySelected,
            onClick = {
                monthlySelected = true
                onChange(true)
            },
        )
        Tab(
            text = { Text(
                stringResource(Res.string.last_year),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) },
            selected = !monthlySelected,
            onClick = {
                monthlySelected = false
                onChange(false)
            }
        )
    }
}
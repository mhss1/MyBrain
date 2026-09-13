package com.mhss.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

val LazyPagingItems<*>.isEmpty: Boolean
    get() = itemCount == 0 && loadState.refresh is LoadState.NotLoading

@Composable
fun PagingLoadState(items: LazyPagingItems<*>) {
    val states = listOf(items.loadState.refresh, items.loadState.prepend, items.loadState.append)
    val loading = items.itemCount == 0 && states.any { it is LoadState.Loading }
    if (loading) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
        }
    }
}

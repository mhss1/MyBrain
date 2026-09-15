package com.mhss.app.domain

import androidx.paging.PagingConfig

const val EVENTS_LIST_MAX_DAYS = 150

val EventsListPagingConfig = PagingConfig(
    pageSize = 30,
    initialLoadSize = 60,
    prefetchDistance = 20,
    maxSize = 120,
    enablePlaceholders = true
)

package com.mhss.app.database

import androidx.paging.PagingConfig

val DefaultPagingConfig = PagingConfig(
    pageSize = 30,
    initialLoadSize = 60,
    prefetchDistance = 20,
    maxSize = 120,
    enablePlaceholders = true,
)

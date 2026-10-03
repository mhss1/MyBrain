package com.mhss.app.database

import androidx.paging.PagingConfig

val DefaultPagingConfig = PagingConfig(
    pageSize = 20,
    initialLoadSize = 30,
    prefetchDistance = 15,
    maxSize = 100,
    enablePlaceholders = true,
)

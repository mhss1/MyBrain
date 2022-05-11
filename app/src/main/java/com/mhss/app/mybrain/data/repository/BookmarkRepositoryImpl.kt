package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class BookmarkRepositoryImpl (
    private val bookmarkDao: BookmarkDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BookmarkRepository
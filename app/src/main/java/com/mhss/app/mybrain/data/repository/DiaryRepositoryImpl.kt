package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.domain.repository.DiaryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DiaryRepositoryImpl (
    private val diaryDao: DiaryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DiaryRepository
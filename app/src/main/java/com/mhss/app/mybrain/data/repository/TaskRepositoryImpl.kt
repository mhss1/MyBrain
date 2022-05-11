package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class TaskRepositoryImpl (
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository

package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class NoteRepositoryImpl (
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NoteRepository
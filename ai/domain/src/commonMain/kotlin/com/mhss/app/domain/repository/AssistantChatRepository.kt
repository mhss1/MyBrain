package com.mhss.app.domain.repository

import androidx.paging.PagingData

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AssistantThread
import kotlinx.coroutines.flow.Flow

interface AssistantChatRepository {
    fun getPagedThreads(): Flow<PagingData<AssistantThread>>

    fun getAllThreads(): Flow<List<AssistantThread>>
    suspend fun saveThread(thread: AssistantThread)
    suspend fun deleteThread(threadId: String)
    suspend fun deleteAllThreads()

    fun getMessages(threadId: String): Flow<List<AiMessage>>
    suspend fun saveMessage(threadId: String, message: AiMessage)
    suspend fun deleteMessage(messageId: String)
}

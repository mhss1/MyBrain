package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class GetAssistantThreadsUseCase(
    private val repository: AssistantChatRepository
) {
    fun paged() = repository.getPagedThreads()
}

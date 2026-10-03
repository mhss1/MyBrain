package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import org.koin.core.annotation.Single

@Single
class SearchEntriesUseCase(
    private val repository: DiaryRepository
) {
    fun paged(query: String) = repository.searchPagedEntries(query)

    suspend operator fun invoke(query: String) = repository.searchEntries(query)
}
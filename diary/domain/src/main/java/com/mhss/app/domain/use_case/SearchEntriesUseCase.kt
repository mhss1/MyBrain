package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import org.koin.core.annotation.Single

@Single
class SearchEntriesUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(query: String) = repository.searchEntries(query)
}
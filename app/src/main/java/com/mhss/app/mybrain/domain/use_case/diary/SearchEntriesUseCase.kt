package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.repository.DiaryRepository
import org.koin.core.annotation.Single

@Single
class SearchEntriesUseCase(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(query: String) = repository.searchEntries(query)
}
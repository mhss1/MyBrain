package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.preferences.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetAllEntriesUseCase(
    private val diaryRepository: DiaryRepository
) {
    fun paged(sortOrder: SortOrder) = diaryRepository.getPagedEntries(sortOrder)

    operator fun invoke(sortOrder: SortOrder) : Flow<List<DiaryEntry>> {
        return diaryRepository.getAllEntries(sortOrder)
    }
}

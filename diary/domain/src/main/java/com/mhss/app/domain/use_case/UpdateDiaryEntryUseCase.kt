package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.model.DiaryEntry
import org.koin.core.annotation.Single

@Single
class UpdateDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry) = diaryRepository.updateEntry(entry)
}
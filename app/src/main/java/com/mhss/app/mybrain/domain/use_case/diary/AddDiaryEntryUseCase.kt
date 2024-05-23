package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.model.diary.DiaryEntry
import com.mhss.app.mybrain.domain.repository.diary.DiaryRepository
import org.koin.core.annotation.Single

@Single
class AddDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry) = diaryRepository.addEntry(entry)
}
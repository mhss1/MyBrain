package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.repository.DiaryRepository
import javax.inject.Inject

class AddDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry) = diaryRepository.addEntry(entry)
}
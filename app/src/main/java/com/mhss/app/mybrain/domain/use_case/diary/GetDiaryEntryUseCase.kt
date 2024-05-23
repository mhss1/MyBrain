package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.repository.diary.DiaryRepository
import org.koin.core.annotation.Single

@Single
class GetDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: Int) = diaryRepository.getEntry(id)
}
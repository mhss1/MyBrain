package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import org.koin.core.annotation.Single

@Single
class GetDiaryEntryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: Int) = diaryRepository.getEntry(id)
}
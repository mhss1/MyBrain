package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.repository.DiaryRepository
import javax.inject.Inject

class GetAllEntriesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke() = diaryRepository.getAllEntries()
}
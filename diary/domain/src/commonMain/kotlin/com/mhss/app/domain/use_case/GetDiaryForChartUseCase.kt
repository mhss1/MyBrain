package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.DiaryRepository
import org.koin.core.annotation.Single

@Single
class GetDiaryForChartUseCase(
    private val diaryRepository: DiaryRepository,
) {
    operator fun invoke(from: Long, to: Long) = diaryRepository.getChartPoints(from, to)
}

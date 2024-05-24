package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.model.diary.DiaryEntry
import com.mhss.app.mybrain.domain.repository.diary.DiaryRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single
class GetDiaryForChartUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(filterSelector: (DiaryEntry) -> Boolean) : List<DiaryEntry>{
        return diaryRepository
            .getAllEntries()
            .first()
            .filter(filterSelector)
            .sortedBy { it.createdDate }
    }
}

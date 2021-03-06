package com.mhss.app.mybrain.domain.use_case.diary

import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.repository.DiaryRepository
import com.mhss.app.mybrain.util.date.inTheLast30Days
import com.mhss.app.mybrain.util.date.inTheLastYear
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetDiaryForChartUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(monthly: Boolean) : List<DiaryEntry>{
        return diaryRepository
            .getAllEntries()
            .first()
            .filter {
                if (monthly) it.createdDate.inTheLast30Days()
                else it.createdDate.inTheLastYear()
            }
            .sortedBy { it.createdDate }
    }
}

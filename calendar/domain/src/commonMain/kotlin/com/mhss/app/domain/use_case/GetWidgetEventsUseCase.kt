package com.mhss.app.domain.use_case

import org.koin.core.annotation.Single

@Single
class GetWidgetEventsUseCase(
    private val getAllEvents: GetAllEventsUseCase
) {
    suspend operator fun invoke(excluded: List<Int>): GetAllEventsResult {
        return getAllEvents(excluded = excluded, fromWidget = true)
    }
}

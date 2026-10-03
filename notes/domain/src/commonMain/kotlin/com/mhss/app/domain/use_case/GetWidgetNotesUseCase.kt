package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.widget.WIDGET_ITEM_LIMIT
import org.koin.core.annotation.Factory

@Factory
class GetWidgetNotesUseCase(
    private val notesRepository: NoteRepository
) {
    operator fun invoke(sortOrder: SortOrder, showAllNotes: Boolean) =
        notesRepository.getLimitedNotes(sortOrder, showAllNotes, WIDGET_ITEM_LIMIT)
}

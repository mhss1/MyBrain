package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetAllNotesUseCase(
    private val notesRepository: NoteRepository
) {
    operator fun invoke(sortOrder: SortOrder, showAllNotes: Boolean): Flow<List<Note>> {
        return if (showAllNotes) {
            notesRepository.getAllNotes(sortOrder)
        } else {
            notesRepository.getAllFolderlessNotes(sortOrder)
        }
    }
}

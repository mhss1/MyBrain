package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.SortOrder
import org.koin.core.annotation.Factory

@Factory
class GetNotesByFolderUseCase(
    private val notesRepository: NoteRepository
) {
    operator fun invoke(id: String, sortOrder: SortOrder) = notesRepository.getNotesByFolder(id, sortOrder)
}

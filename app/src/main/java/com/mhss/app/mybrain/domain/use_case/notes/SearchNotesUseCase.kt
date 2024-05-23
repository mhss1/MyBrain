package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class SearchNotesUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(query: String) = notesRepository.searchNotes(query)
}
package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.NoteRepository
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(query: String) = notesRepository.searchNotes(query)
}
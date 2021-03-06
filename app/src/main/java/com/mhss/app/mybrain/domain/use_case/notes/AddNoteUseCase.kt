package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = notesRepository.addNote(note)
}
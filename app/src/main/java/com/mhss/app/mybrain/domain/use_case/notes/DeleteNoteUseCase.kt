package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.notes.Note
import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.deleteNote(note)
}
package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteFolderUseCass @Inject constructor(
private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(folder: NoteFolder) = noteRepository.updateNoteFolder(folder)
}
package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.NoteRepository
import javax.inject.Inject

class GetNoteFolderUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(folderId: Int) = repository.getNoteFolder(folderId)
}
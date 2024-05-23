package com.mhss.app.mybrain.domain.use_case.notes

import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import org.koin.core.annotation.Single

@Single
class GetAllNoteFoldersUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke() = repository.getAllNoteFolders()
}
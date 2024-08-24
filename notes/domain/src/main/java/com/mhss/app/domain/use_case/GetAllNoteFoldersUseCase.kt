package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Single

@Single
class GetAllNoteFoldersUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke() = repository.getAllNoteFolders()
}
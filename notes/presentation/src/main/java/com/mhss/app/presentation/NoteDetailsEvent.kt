package com.mhss.app.presentation

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder

sealed class NoteDetailsEvent {
    data class DeleteNote(val note: Note) : NoteDetailsEvent()
    data object ToggleReadingMode : NoteDetailsEvent()
    data class Summarize(val content: String): NoteDetailsEvent(), AiAction
    data class AutoFormat(val content: String): NoteDetailsEvent(), AiAction
    data class CorrectSpelling(val content: String): NoteDetailsEvent(), AiAction
    data object AiResultHandled: NoteDetailsEvent()
    data object ScreenOnStop: NoteDetailsEvent()
    data class UpdateTitle(val title: String): NoteDetailsEvent()
    data class UpdateContent(val content: String): NoteDetailsEvent()
    data class UpdateFolder(val folder: NoteFolder?): NoteDetailsEvent()
    data class UpdatePinned(val pinned: Boolean): NoteDetailsEvent()
}

sealed interface AiAction
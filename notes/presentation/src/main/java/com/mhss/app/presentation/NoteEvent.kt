package com.mhss.app.presentation

import com.mhss.app.domain.model.*
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.ui.ItemView

sealed class NoteEvent {
    data class AddNote(val note: Note) : NoteEvent()
    data class SearchNotes(val query: String) : NoteEvent()
    data class UpdateOrder(val order: Order) : NoteEvent()
    data class UpdateView(val view: ItemView) : NoteEvent()
    data object ErrorDisplayed: NoteEvent()
    data class CreateFolder(val folder: NoteFolder): NoteEvent()
    data class DeleteFolder(val folder: NoteFolder): NoteEvent()
    data class UpdateFolder(val folder: NoteFolder): NoteEvent()
    data class GetFolderNotes(val id: Int): NoteEvent()
    data class GetFolder(val id: Int): NoteEvent()
}
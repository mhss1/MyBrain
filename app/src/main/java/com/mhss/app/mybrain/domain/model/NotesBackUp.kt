package com.mhss.app.mybrain.domain.model

data class NotesBackUp(
    val notes: List<Note>,
    val folders: List<NoteFolder>
)

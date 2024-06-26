package com.mhss.app.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mhss.app.app.R
import com.mhss.app.domain.model.Note
import com.mhss.app.util.date.now
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddNoteFromShareActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val content = intent.getStringExtra(Intent.EXTRA_TEXT)
                val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                if (!content.isNullOrBlank()) {
                    viewModel.onEvent(
                        NoteEvent.AddNote(
                            Note(
                                title = title ?: "",
                                content = content,
                                createdDate = now(),
                                updatedDate = now()
                            )
                        )
                    )
                    Toast.makeText(this, getString(R.string.added_note), Toast.LENGTH_SHORT)
                        .show()
                } else
                    Toast.makeText(this, getString(R.string.error_empty_title), Toast.LENGTH_SHORT)
                        .show()
            }
        }
        finish()
    }
}
package com.mhss.app.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.util.date.now
import org.koin.androidx.viewmodel.ext.android.viewModel

class SaveBookmarkActivity : ComponentActivity() {

    private val viewModel: BookmarksViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val url = intent.getStringExtra(Intent.EXTRA_TEXT)
                val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                if (!url.isNullOrBlank()) {
                    if (url.isValidUrl()) {
                        viewModel.onEvent(
                            BookmarkEvent.AddBookmark(
                                Bookmark(
                                    url = url.trim(),
                                    title = title ?: "",
                                    createdDate = now(),
                                    updatedDate = now()
                                )
                            )
                        )
                        Toast.makeText(this, getString(R.string.bookmark_saved), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }
}
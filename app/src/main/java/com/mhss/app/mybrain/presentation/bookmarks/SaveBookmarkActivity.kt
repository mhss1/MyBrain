package com.mhss.app.mybrain.presentation.bookmarks

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import com.mhss.app.mybrain.util.bookmarks.isValidUrl
import com.mhss.app.mybrain.util.date.now
import org.koin.androidx.viewmodel.ext.android.viewModel

class SaveBookmarkActivity : ComponentActivity() {

    private val viewModel: BookmarksViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val url = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!url.isNullOrBlank()) {
                    if (url.isValidUrl()) {
                        viewModel.onEvent(
                            BookmarkEvent.AddBookmark(
                                Bookmark(
                                    url = url.trim(),
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
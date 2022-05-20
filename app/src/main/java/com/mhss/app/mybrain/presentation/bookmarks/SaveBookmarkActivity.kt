package com.mhss.app.mybrain.presentation.bookmarks

import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.Bookmark
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaveBookmarkActivity : ComponentActivity() {

    private val viewModel: BookmarksViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val url = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!url.isNullOrBlank()) {
                    if (URLUtil.isValidUrl(url)) {
                        viewModel.onEvent(
                            BookmarkEvent.AddBookmark(
                                Bookmark(
                                    url = url.trim(),
                                    createdDate = System.currentTimeMillis(),
                                    updatedDate = System.currentTimeMillis()
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
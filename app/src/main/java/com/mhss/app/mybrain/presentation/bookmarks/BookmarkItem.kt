package com.mhss.app.mybrain.presentation.bookmarks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import com.mhss.app.mybrain.util.bookmarks.isValidUrl

@Composable
fun BookmarkItem(
    modifier: Modifier = Modifier,
    bookmark: Bookmark,
    onClick: (Bookmark) -> Unit,
    onInvalidUrl: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .clickable { onClick(bookmark) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            if (bookmark.title.isNotBlank()){
                Text(
                    bookmark.title,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
            }
            Row {

            }
            Text(
                bookmark.url,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            IconButton(
                onClick = {
                    if (bookmark.url.isValidUrl()){
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(if (!bookmark.url.startsWith("http://") && !bookmark.url.startsWith("https://")) "http://${bookmark.url}" else bookmark.url)
                        context.startActivity(intent)
                    } else
                        onInvalidUrl()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_open_link),
                    stringResource(id = R.string.open_link)
                )
            }
        }
    }
}
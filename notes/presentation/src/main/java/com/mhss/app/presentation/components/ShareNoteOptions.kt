package com.mhss.app.presentation.components

import android.content.Intent
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mhss.app.ui.R

@Composable
fun ShareNoteAsPlainTextOption(
    title: String,
    content: String,
    onOptionSelected: () -> Unit
) {
    val context = LocalContext.current
    DropdownMenuItem(
        text = { Text(stringResource(R.string.plain_text)) },
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
            onOptionSelected()
        },
        leadingIcon = {
            Icon(
                painterResource(id = R.drawable.ic_plain_text),
                contentDescription = stringResource(R.string.plain_text)
            )
        }
    )
}
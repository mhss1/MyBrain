package com.mhss.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mhss.app.app.R

@Composable
fun AiSummarizeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GradientIconButton(
        modifier = modifier,
        text = stringResource(id = R.string.summarize),
        iconPainter = painterResource(id = R.drawable.ic_summarize),
        onClick = onClick
    )
}
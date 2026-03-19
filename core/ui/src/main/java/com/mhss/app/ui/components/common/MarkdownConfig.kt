package com.mhss.app.ui.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.m3.markdownTypography


/**
 * Preserves blank-line spacing for the Markdown renderer by inserting
 * non-breaking-space paragraphs for consecutive blank lines (which Markdown normally
 * collapses into one). Regular newlines between content lines are left untouched.
 * Fenced code blocks are passed through unchanged.
 */
fun String.withHardLineBreaks(): String = buildString(length + length / 2) {
    val content = this@withHardLineBreaks
    var inFencedBlock = false
    var pendingBlanks = 0
    var isFirst = true
    var i = 0

    fun flushBlanks() {
        append("\n\n")
        repeat(maxOf(0, pendingBlanks - 1)) { append("\u00A0\n\n") }
        pendingBlanks = 0
    }

    while (i <= content.length) {
        val eol = content.indexOf('\n', i).let { if (it == -1) content.length else it }
        val isFence = content.isFenceAt(i, eol)

        if (inFencedBlock) {
            append('\n')
            append(content, i, eol)
            if (isFence) inFencedBlock = false
        } else if (i == eol) {
            pendingBlanks++
        } else {
            if (!isFirst) {
                appendLine()
                if (pendingBlanks > 0) flushBlanks() else append('\n')
            }
            isFirst = false
            append(content, i, eol)
            if (isFence) inFencedBlock = true
        }

        i = eol + 1
    }

    if (pendingBlanks > 0) flushBlanks()
}

/** Checks if the line at [start]..[end] starts with a ``` fence marker (ignoring leading spaces). */
private fun String.isFenceAt(start: Int, end: Int): Boolean {
    var i = start
    while (i < end && this[i] == ' ') i++
    return i + 3 <= end && this[i] == '`' && this[i + 1] == '`' && this[i + 2] == '`'
}

@Composable
fun defaultMarkdownTypography() = markdownTypography(
    text = MaterialTheme.typography.bodyMedium,
    bullet = MaterialTheme.typography.bodyMedium,
    ordered = MaterialTheme.typography.bodyMedium,
    list = MaterialTheme.typography.bodyMedium,
    code = MaterialTheme.typography.bodyMedium,
    h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    textLink = TextLinkStyles(
        MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        ).toSpanStyle()
    )
)


@Composable
fun previewMarkdownTypography() = markdownTypography(
    text = MaterialTheme.typography.labelMedium,
    bullet = MaterialTheme.typography.labelMedium,
    ordered = MaterialTheme.typography.labelMedium,
    list = MaterialTheme.typography.labelMedium,
    code = MaterialTheme.typography.labelMedium,
    h1 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    h3 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    h4 = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
    h5 = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
    h6 = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
    textLink = TextLinkStyles(
        MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        ).toSpanStyle()
    )
)
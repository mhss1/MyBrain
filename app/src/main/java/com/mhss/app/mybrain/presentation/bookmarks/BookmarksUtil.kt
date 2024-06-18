package com.mhss.app.mybrain.presentation.bookmarks

fun String.isValidUrl(): Boolean {
    val urlRegex = "^(https?://)?([\\w.-]+\\.[a-z]{2,})(:\\d+)?(/[^?#]*)?(\\?[^#]*)?(#.*)?$".toRegex(RegexOption.IGNORE_CASE)
    return urlRegex.matches(this)
}
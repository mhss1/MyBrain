package com.mhss.app.mybrain.presentation.diary

import androidx.compose.ui.graphics.Color
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.diary.Mood
import com.mhss.app.mybrain.ui.theme.*

val Mood.iconRes: Int
    get() = when (this) {
        Mood.AWESOME -> R.drawable.ic_very_happy
        Mood.GOOD -> R.drawable.ic_happy
        Mood.OKAY -> R.drawable.ic_ok_face
        Mood.BAD -> R.drawable.ic_sad
        Mood.TERRIBLE -> R.drawable.ic_very_sad
    }

val Mood.color: Color
    get() = when (this) {
        Mood.AWESOME -> Green
        Mood.GOOD -> Blue
        Mood.OKAY -> Purple
        Mood.BAD -> Orange
        Mood.TERRIBLE -> Color.Red
    }

val Mood.titleRes: Int
    get() = when (this) {
        Mood.AWESOME -> R.string.awesome
        Mood.GOOD -> R.string.good
        Mood.OKAY -> R.string.okay
        Mood.BAD -> R.string.bad
        Mood.TERRIBLE -> R.string.terrible
    }
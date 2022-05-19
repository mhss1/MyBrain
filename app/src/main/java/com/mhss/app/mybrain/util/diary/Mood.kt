package com.mhss.app.mybrain.util.diary

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.ui.theme.Blue
import com.mhss.app.mybrain.ui.theme.DarkOrange
import com.mhss.app.mybrain.ui.theme.Green
import com.mhss.app.mybrain.ui.theme.Purple

enum class Mood(@DrawableRes val icon: Int, val color: Color, @StringRes val title: Int) {
    AWESOME(R.drawable.ic_very_happy, Green, R.string.awesome),
    GOOD(R.drawable.ic_happy, Blue, R.string.good),
    OKAY(R.drawable.ic_ok_face, Purple, R.string.okay),
    BAD(R.drawable.ic_sad, DarkOrange, R.string.bad),
    TERRIBLE(R.drawable.ic_very_sad, Color.Red, R.string.terrible)
}
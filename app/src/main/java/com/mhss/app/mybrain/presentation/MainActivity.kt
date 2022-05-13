package com.mhss.app.mybrain.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mhss.app.mybrain.ui.theme.DarkBackground
import com.mhss.app.mybrain.ui.theme.MyBrainTheme
import com.mhss.app.mybrain.util.settings.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themMode by remember { mutableStateOf(ThemeSettings.AUTO)} // TODO: get this from the settings
            val isDarkMode = when (themMode) {
                ThemeSettings.AUTO -> isSystemInDarkTheme()
                ThemeSettings.DARK -> true
                ThemeSettings.LIGHT -> false
            }
            handleThemeChange(isDarkMode)
            MyBrainTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                }
            }
        }
    }
    private fun handleThemeChange(isDarkMode: Boolean) {
        window.statusBarColor = if (isDarkMode) DarkBackground.toArgb() else Color.White.toArgb()
        window.navigationBarColor = if (isDarkMode) DarkBackground.toArgb() else Color.White.toArgb()
    }
}
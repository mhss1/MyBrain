package com.mhss.app.mybrain.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.common.ThemeSettings
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNotificationPermissionGranted())
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        val appLockManager = AppLockManager(this)
        setContent {
            val blockScreenshots by viewModel.blockScreenshots.collectAsState(initial = false)
            val themeMode = viewModel.themeMode.collectAsState(initial = ThemeSettings.AUTO.value)

            LaunchedEffect(blockScreenshots) {
                if (blockScreenshots) {
                    window.setFlags(
                        LayoutParams.FLAG_SECURE,
                        LayoutParams.FLAG_SECURE
                    )
                } else
                    window.clearFlags(LayoutParams.FLAG_SECURE)
            }

            val isDarkMode = when (themeMode.value) {
                ThemeSettings.DARK.value -> true
                ThemeSettings.LIGHT.value -> false
                else -> isSystemInDarkTheme()
            }
            LaunchedEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                        detectDarkMode = {
                            isDarkMode
                        }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                        detectDarkMode = {
                            isDarkMode
                        }
                    ),
                )
            }
            MyBrainApp(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                appLockManager = appLockManager
            )
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
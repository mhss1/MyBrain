package com.mhss.app.mybrain.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.mybrain.MyBrainApp
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.ui.ThemeSettings
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    @SuppressLint("FlowOperatorInvokedInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = buildList {
            if (!isNotificationPermissionGranted()) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissions.toTypedArray(),
                0
            )
        }
        val appLockManager = AppLockManager(this)
        setContent {
            val blockScreenshots by viewModel.blockScreenshots.collectAsState(initial = false)
            val isSystemDarkMode = isSystemInDarkTheme()
            val isDarkMode by viewModel.themeMode
                .map {
                    it == ThemeSettings.DARK.value || (it == ThemeSettings.AUTO.value && isSystemDarkMode)
                }.collectAsStateWithLifecycle(true)

            LaunchedEffect(blockScreenshots) {
                if (blockScreenshots) {
                    window.setFlags(
                        LayoutParams.FLAG_SECURE,
                        LayoutParams.FLAG_SECURE
                    )
                } else
                    window.clearFlags(LayoutParams.FLAG_SECURE)
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
            CompositionLocalProvider(
                LocalDateTimeFormatter provides dateTimeFormatter
            ) {
                MyBrainApp(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    appLockManager = appLockManager
                )
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStart() {
        super.onStart()
        viewModel.syncAll()
    }
}

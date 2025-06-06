package com.mhss.app.mybrain.presentation.main

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.util.Constants
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.mybrain.BuildConfig
import com.mhss.app.ui.R
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.preferences.domain.model.*
import com.mhss.app.presentation.SettingsBasicLinkItem
import com.mhss.app.presentation.SettingsItemCard
import com.mhss.app.presentation.SettingsSwitchCard
import com.mhss.app.presentation.SettingsViewModel
import com.mhss.app.ui.FontSizeSettings
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.getName
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Rubik
import com.mhss.app.ui.getFontSizeName
import com.mhss.app.ui.toFontFamily
import com.mhss.app.ui.toInt
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    appLockManager: AppLockManager,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val showMaterialYouOption = remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(stringResource(R.string.settings))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val theme by viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                        ThemeSettings.AUTO.value
                    ).collectAsStateWithLifecycle(ThemeSettings.AUTO.value)
                ThemeSettingsItem(theme) {
                    when (theme) {
                        ThemeSettings.AUTO.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.LIGHT.value
                        )

                        ThemeSettings.LIGHT.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.DARK.value
                        )

                        ThemeSettings.DARK.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.AUTO.value
                        )
                    }
                }
            }
            item {
                val screen by viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                        StartUpScreenSettings.SPACES.value
                    ).collectAsStateWithLifecycle(StartUpScreenSettings.SPACES.value)
                StartUpScreenSettingsItem(
                    screen
                ) { screenValue ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                        screenValue
                    )
                }
            }
            item {
                val screen = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.APP_FONT_KEY),
                        Rubik.toInt()
                    ).collectAsStateWithLifecycle(Rubik.toInt())
                AppFontSettingsItem(
                    screen.value,
                ) { font ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.APP_FONT_KEY),
                        font
                    )
                }
            }
            item {
                val fontSize = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.FONT_SIZE_KEY),
                        FontSizeSettings.NORMAL.value
                    ).collectAsStateWithLifecycle(FontSizeSettings.NORMAL.value)
                FontSizeSettingsItem(
                    fontSize.value,
                ) { fontSizeValue ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.FONT_SIZE_KEY),
                        fontSizeValue
                    )
                }
            }
            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY),
                        false
                    ).collectAsStateWithLifecycle(false)
                SettingsSwitchCard(
                    text = stringResource(R.string.block_screenshots),
                    checked = block.value,
                    painterResource(R.drawable.ic_block_screenshot)
                ) {
                    viewModel.saveSettings(
                        booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY),
                        it
                    )
                }
            }

            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY),
                        false
                    ).collectAsStateWithLifecycle(false)
                SettingsSwitchCard(
                    text = stringResource(R.string.lock_app),
                    checked = block.value,
                    iconPainter = painterResource(R.drawable.ic_lock)
                ) {
                    if (appLockManager.canUseFeature()) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY),
                            it
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.no_auth_method)
                            )
                        }
                    }
                }
            }


            if (showMaterialYouOption) {
                item {
                    val block = viewModel
                        .getSettings(
                            booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                            false
                        ).collectAsStateWithLifecycle(false)
                    SettingsSwitchCard(
                        text = stringResource(R.string.material_you),
                        checked = block.value,
                        iconPainter = painterResource(R.drawable.ic_palette)
                    ) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                            it
                        )
                    }
                }
            }
            item {
                SettingsBasicLinkItem(
                    title = R.string.integrations,
                    icon = R.drawable.ic_integrations,
                    onClick = {
                        navController.navigate(Screen.IntegrationsScreen)
                    }
                )
            }
            item {
                SettingsBasicLinkItem(
                    title = R.string.export_import,
                    icon = R.drawable.ic_import_export,
                    onClick = {
                        navController.navigate(Screen.ImportExportScreen)
                    }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = R.string.app_version,
                    icon = R.drawable.ic_code,
                    subtitle = context.getPackageInfo().versionName ?: BuildConfig.VERSION_NAME,
                    link = Constants.GITHUB_RELEASES_LINK
                )
            }
            item {
                SettingsBasicLinkItem(
                    title = R.string.project_on_github,
                    icon = R.drawable.ic_github,
                    link = Constants.PROJECT_GITHUB_LINK
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = R.string.privacy_policy,
                    icon = R.drawable.ic_privacy,
                    link = Constants.PRIVACY_POLICY_LINK
                )
            }

            item {
                Text(
                    text = stringResource(R.string.product),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = R.string.request_feature_report_bug,
                    icon = R.drawable.ic_feature_issue,
                    link = Constants.GITHUB_ISSUES_LINK
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = R.string.project_roadmap,
                    icon = R.drawable.ic_roadmap,
                    link = Constants.PROJECT_ROADMAP_LINK
                )
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun ThemeSettingsItem(theme: Int = 0, onClick: () -> Unit = {}) {
    SettingsItemCard(
        onClick = onClick,
        cornerRadius = 18.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paint_roller),
                contentDescription = stringResource(R.string.app_theme),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.app_theme),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        val themeTextId = remember(theme) {
            when (theme) {
                ThemeSettings.LIGHT.value -> R.string.light_theme
                ThemeSettings.DARK.value -> R.string.dark_theme
                else -> R.string.auto_theme
            }
        }
        val themePainterId = remember(theme) {
            when (theme) {
                ThemeSettings.LIGHT.value -> R.drawable.ic_sun
                ThemeSettings.DARK.value -> R.drawable.ic_dark
                else -> R.drawable.ic_auto
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(themeTextId, label = "themeTex") { id ->
                Text(
                    text = stringResource(id),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedContent(themePainterId, label = "themePainter") { id ->
                Icon(
                    painter = painterResource(id),
                    contentDescription = stringResource(themeTextId),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StartUpScreenSettingsItem(
    screen: Int,
    onScreenChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = stringResource(R.string.start_up_screen),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.start_up_screen),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (screen) {
                        StartUpScreenSettings.SPACES.value -> stringResource(R.string.spaces)
                        StartUpScreenSettings.DASHBOARD.value -> stringResource(R.string.dashboard)
                        StartUpScreenSettings.NOTES.value -> stringResource(R.string.notes)
                        StartUpScreenSettings.TASKS.value -> stringResource(R.string.tasks)
                        StartUpScreenSettings.DIARY.value -> stringResource(R.string.diary)
                        StartUpScreenSettings.BOOKMARKS.value -> stringResource(R.string.bookmarks)
                        StartUpScreenSettings.CALENDAR.value -> stringResource(R.string.calendar)
                        StartUpScreenSettings.ASSISTANT.value -> stringResource(R.string.assistant)
                        else -> stringResource(R.string.spaces)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                val options = listOf(
                    StartUpScreenSettings.SPACES to R.string.spaces,
                    StartUpScreenSettings.DASHBOARD to R.string.dashboard,
                    StartUpScreenSettings.NOTES to R.string.notes,
                    StartUpScreenSettings.TASKS to R.string.tasks,
                    StartUpScreenSettings.DIARY to R.string.diary,
                    StartUpScreenSettings.BOOKMARKS to R.string.bookmarks,
                    StartUpScreenSettings.CALENDAR to R.string.calendar,
                    StartUpScreenSettings.ASSISTANT to R.string.assistant
                )
                
                options.forEach { (screenOption, stringRes) ->
                    DropdownMenuItem(
                        onClick = {
                            onScreenChange(screenOption.value)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(id = stringRes),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppFontSettingsItem(
    selectedFont: Int,
    onFontChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val fonts = listOf(
        FontFamily.Default,
        Rubik,
        FontFamily.Monospace,
        FontFamily.SansSerif
    )
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_font),
                contentDescription = stringResource(R.string.app_font),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.app_font),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedFont.toFontFamily().getName(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                fonts.forEach {
                    DropdownMenuItem(onClick = {
                        onFontChange(it.toInt())
                        expanded = false
                    },
                        text = {
                            Text(
                                text = it.getName(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        })
                }
            }
        }
    }
}

@Composable
fun FontSizeSettingsItem(
    selectedFontSize: Int,
    onFontSizeChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val fontSizes = FontSizeSettings.entries
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_font_size),
                contentDescription = stringResource(R.string.font_size),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.font_size),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedFontSize.getFontSizeName(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                fontSizes.forEach { fontSizeItem ->
                    DropdownMenuItem(
                        onClick = {
                            onFontSizeChange(fontSizeItem.value)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(fontSizeItem.title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}

fun Context.getPackageInfo(): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
}
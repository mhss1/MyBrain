package com.mhss.app.mybrain.presentation.main

import android.os.Build
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
import androidx.compose.ui.text.font.FontWeight
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
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.getName
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Rubik
import com.mhss.app.ui.toFontFamily
import com.mhss.app.ui.toInt
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = paddingValues) {
            item {
                val theme = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY), ThemeSettings.AUTO.value
                    ).collectAsStateWithLifecycle(ThemeSettings.AUTO.value)
                ThemeSettingsItem(theme.value) {
                    when (theme.value) {
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
                val screen = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                        StartUpScreenSettings.SPACES.value
                    ).collectAsStateWithLifecycle(StartUpScreenSettings.SPACES.value)
                StartUpScreenSettingsItem(
                    screen.value,
                    {
                        viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                            StartUpScreenSettings.SPACES.value
                        )
                    },
                    {
                        viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                            StartUpScreenSettings.DASHBOARD.value
                        )
                    }
                )
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
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY),
                        false
                    ).collectAsStateWithLifecycle(false)
                SettingsSwitchCard(
                    stringResource(R.string.block_screenshots),
                    block.value
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
                    stringResource(R.string.lock_app),
                    block.value
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
                        stringResource(R.string.material_you),
                        block.value
                    ) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                            it
                        )
                    }
                }
            }
            item {
                SettingsItemCard(
                    cornerRadius = 16.dp,
                    onClick = {
                        navController.navigate(Screen.IntegrationsScreen)
                    }
                ) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_integrations),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.integrations),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
            item {
                SettingsItemCard(
                    cornerRadius = 16.dp,
                    onClick = {
                        navController.navigate(Screen.ImportExportScreen)
                    }
                ) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_import_export),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.export_import),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
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
                    subtitle = BuildConfig.VERSION_NAME,
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
        cornerRadius = 18.dp
    ) {
        Text(
            text = stringResource(R.string.app_theme),
            style = MaterialTheme.typography.titleLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (theme) {
                    ThemeSettings.LIGHT.value -> stringResource(R.string.light_theme)
                    ThemeSettings.DARK.value -> stringResource(R.string.dark_theme)
                    else -> stringResource(R.string.auto_theme)
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = when (theme) {
                    ThemeSettings.LIGHT.value -> painterResource(id = R.drawable.ic_sun)
                    ThemeSettings.DARK.value -> painterResource(id = R.drawable.ic_dark)
                    else -> painterResource(id = R.drawable.ic_auto)
                },
                contentDescription = theme.toString()
            )
        }
    }
}

@Composable
fun StartUpScreenSettingsItem(
    screen: Int,
    onSpacesClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Text(
            text = stringResource(R.string.start_up_screen),
            style = MaterialTheme.typography.titleLarge
        )
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
                DropdownMenuItem(onClick = {
                    onSpacesClick()
                    expanded = false
                },
                    text = {
                        Text(
                            text = stringResource(id = R.string.spaces),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    })
                DropdownMenuItem(onClick = {
                    onDashboardClick()
                    expanded = false
                },
                    text = {
                        Text(
                            text = stringResource(id = R.string.dashboard),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    })
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
        Text(
            text = stringResource(R.string.app_font),
            style = MaterialTheme.typography.titleLarge
        )
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
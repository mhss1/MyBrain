package com.mhss.app.mybrain.presentation.main

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
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
import com.mhss.app.mybrain.domain.model.preferences.booleanPreferencesKey
import com.mhss.app.mybrain.domain.model.preferences.intPreferencesKey
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.BuildConfig
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.settings.SettingsBasicLinkItem
import com.mhss.app.mybrain.presentation.settings.SettingsItemCard
import com.mhss.app.mybrain.presentation.settings.SettingsSwitchCard
import com.mhss.app.mybrain.presentation.settings.SettingsViewModel
import com.mhss.app.mybrain.presentation.navigation.Screen
import com.mhss.app.mybrain.ui.theme.Rubik
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
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
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val appLockManager = remember {
            context.getActivity()?.let {
                AppLockManager(it)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = paddingValues) {
            item {
                val theme = viewModel
                    .getSettings(
                        intPreferencesKey(Constants.SETTINGS_THEME_KEY), ThemeSettings.AUTO.value
                    ).collectAsState(
                        initial = ThemeSettings.AUTO.value
                    )
                ThemeSettingsItem(theme.value) {
                    when (theme.value) {
                        ThemeSettings.AUTO.value -> viewModel.saveSettings(
                            intPreferencesKey(Constants.SETTINGS_THEME_KEY),
                            ThemeSettings.LIGHT.value
                        )

                        ThemeSettings.LIGHT.value -> viewModel.saveSettings(
                            intPreferencesKey(Constants.SETTINGS_THEME_KEY),
                            ThemeSettings.DARK.value
                        )

                        ThemeSettings.DARK.value -> viewModel.saveSettings(
                            intPreferencesKey(Constants.SETTINGS_THEME_KEY),
                            ThemeSettings.AUTO.value
                        )
                    }
                }
            }
            item {
                val screen = viewModel
                    .getSettings(
                        intPreferencesKey(Constants.DEFAULT_START_UP_SCREEN_KEY),
                        StartUpScreenSettings.SPACES.value
                    ).collectAsState(
                        initial = StartUpScreenSettings.SPACES.value
                    )
                StartUpScreenSettingsItem(
                    screen.value,
                    {
                        viewModel.saveSettings(
                            intPreferencesKey(Constants.DEFAULT_START_UP_SCREEN_KEY),
                            StartUpScreenSettings.SPACES.value
                        )
                    },
                    {
                        viewModel.saveSettings(
                            intPreferencesKey(Constants.DEFAULT_START_UP_SCREEN_KEY),
                            StartUpScreenSettings.DASHBOARD.value
                        )
                    }
                )
            }
            item {
                val screen = viewModel
                    .getSettings(
                        intPreferencesKey(Constants.APP_FONT_KEY),
                        Rubik.toInt()
                    ).collectAsState(
                        initial = Rubik.toInt()
                    )
                AppFontSettingsItem(
                    screen.value,
                ) { font ->
                    viewModel.saveSettings(
                        intPreferencesKey(Constants.APP_FONT_KEY),
                        font
                    )
                }
            }
            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(Constants.BLOCK_SCREENSHOTS_KEY),
                        false
                    ).collectAsState(
                        initial = false
                    )
                SettingsSwitchCard(
                    stringResource(R.string.block_screenshots),
                    block.value
                ) {
                    viewModel.saveSettings(
                        booleanPreferencesKey(Constants.BLOCK_SCREENSHOTS_KEY),
                        it
                    )
                }
            }

            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(Constants.LOCK_APP_KEY),
                        false
                    ).collectAsState(
                        initial = false
                    )
                SettingsSwitchCard(
                    stringResource(R.string.lock_app),
                    block.value
                ) {
                    if (appLockManager?.canUseFeature() == true) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(Constants.LOCK_APP_KEY),
                            it
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                getString(R.string.no_auth_method)
                            )
                        }
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

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
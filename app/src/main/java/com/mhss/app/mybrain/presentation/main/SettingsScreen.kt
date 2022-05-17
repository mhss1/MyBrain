package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import com.mhss.app.mybrain.BuildConfig
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.settings.DonationItem
import com.mhss.app.mybrain.presentation.settings.SettingsBasicLinkItem
import com.mhss.app.mybrain.presentation.settings.SettingsItemCard
import com.mhss.app.mybrain.presentation.settings.SettingsViewModel
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.ThemeSettings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val openDonationDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = R.string.app_version,
                    icon = R.drawable.ic_code,
                    subtitle = BuildConfig.VERSION_NAME,
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
                    style = MaterialTheme.typography.h5,
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

            item {
                SettingsBasicLinkItem(
                    title = R.string.donate,
                    icon = R.drawable.ic_heart,
                    onClick = { openDonationDialog.value = true }
                )
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
        if (openDonationDialog.value)
            AlertDialog(
                title = { Text(text = stringResource(R.string.thank_you_for_support)) },
                text = { Text(text = stringResource(R.string.donate_with)) },
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDonationDialog.value = false },
                buttons = {
                    LazyRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            DonationItem("PayPal", Constants.PAYPAL_LINK, R.drawable.ic_paypal)
                        }
                        item {
                            DonationItem("BuyMeACoffee", Constants.BUY_ME_A_COFFEE_LINK)
                        }
                        item {
                            DonationItem("Ko-fi", Constants.KO_FI_LINK)
                        }
                    }
                }
            )
    }
}

@Composable
fun ThemeSettingsItem(theme: Int = 0, onClick: () -> Unit = {}) {
    SettingsItemCard(
        onClick = onClick,
    ) {
        Text(
            text = stringResource(R.string.app_theme),
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (theme) {
                    ThemeSettings.LIGHT.value -> stringResource(R.string.light_theme)
                    ThemeSettings.DARK.value -> stringResource(R.string.dark_theme)
                    else -> stringResource(R.string.auto_theme)
                },
                style = MaterialTheme.typography.body1
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
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = when (screen) {
                    StartUpScreenSettings.SPACES.value -> stringResource(R.string.spaces)
                    StartUpScreenSettings.DASHBOARD.value -> stringResource(R.string.dashboard)
                    else -> stringResource(R.string.spaces)
                },
                style = MaterialTheme.typography.body1
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(onClick = {
                    onSpacesClick()
                    expanded = false
                }) {
                    Text(text = stringResource(id = R.string.spaces))
                }
                DropdownMenuItem(onClick = {
                    onDashboardClick()
                    expanded = false
                }) {
                    Text(text = stringResource(id = R.string.dashboard))
                }
            }
        }
    }
}

@Preview
@Composable
fun ThemeItemPreview() {
    ThemeSettingsItem()
}

@Preview
@Composable
fun StartUpItemPreview() {
    StartUpScreenSettingsItem(0)
}
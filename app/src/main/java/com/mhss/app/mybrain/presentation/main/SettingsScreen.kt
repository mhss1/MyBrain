package com.mhss.app.mybrain.presentation.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import com.mhss.app.mybrain.BuildConfig
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.settings.SettingsItemCard
import com.mhss.app.mybrain.presentation.settings.SettingsViewModel
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.StartUpScreenSettings
import com.mhss.app.mybrain.util.settings.ThemeSettings

@ExperimentalAnimationApi
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = MaterialTheme.colors.background
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
                SettingsItemCard {
                    Text(
                        text = stringResource(id = R.string.app_version),
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            item {
                val context = LocalContext.current
                SettingsItemCard(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(Constants.PROJECT_GITHUB_LINK)
                        context.startActivity(intent)
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_github), contentDescription = "github")
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.project_on_github),
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.my_brain_is_open_source),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }

            item {
                val context = LocalContext.current
                SettingsItemCard(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(Constants.PRIVACY_POLICY_LINK)
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.privacy_policy),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                var visible by remember { mutableStateOf(false) }
                SettingsItemCard(
                    onClick = { visible = !visible }
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_heart),
                                contentDescription = stringResource(id = R.string.support_the_app)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.support_the_app),
                                style = MaterialTheme.typography.body1,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedVisibility(visible = visible) {
                            val context = LocalContext.current
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Row(
                                        Modifier
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse(Constants.PAYPAL_LINK)
                                                context.startActivity(intent)
                                            }
                                            .clip(RoundedCornerShape(25.dp))
                                            .border(
                                                color = Color.DarkGray,
                                                width = 1.dp,
                                                shape = RoundedCornerShape(25.dp)
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_paypal),
                                            contentDescription = "PayPal",
                                            modifier = Modifier.padding(4.dp)
                                        )
                                        Text(
                                            text = "PayPal",
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                                item {
                                    Row(Modifier
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.data = Uri.parse(Constants.BUY_ME_A_COFFEE_LINK)
                                            context.startActivity(intent)
                                        }
                                        .clip(RoundedCornerShape(25.dp))
                                        .border(
                                            color = Color.DarkGray,
                                            width = 1.dp,
                                            shape = RoundedCornerShape(25.dp)
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "BuyMeACoffee",
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                                item {
                                    Row(Modifier
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.data = Uri.parse(Constants.KO_FI_LINK)
                                            context.startActivity(intent)
                                        }
                                        .clip(RoundedCornerShape(25.dp))
                                        .border(
                                            color = Color.DarkGray,
                                            width = 1.dp,
                                            shape = RoundedCornerShape(25.dp)
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ko-fi",
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
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
        onClick = {
            expanded = true
        },
    ) {
        Text(
            text = stringResource(R.string.default_start_up_screen),
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
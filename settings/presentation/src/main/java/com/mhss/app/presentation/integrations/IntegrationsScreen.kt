package com.mhss.app.presentation.integrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.app.R
import com.mhss.app.domain.AiConstants
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.integrations),
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    val provider by viewModel.getSettings(
                        PrefsKey.IntKey(PrefsConstants.AI_PROVIDER_KEY),
                        AiProvider.None.ordinal
                    ).collectAsStateWithLifecycle(AiProvider.None.ordinal)

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.ai),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Switch(
                                checked = provider != AiProvider.None.ordinal,
                                onCheckedChange = {
                                    viewModel.saveSettings(
                                        PrefsKey.IntKey(PrefsConstants.AI_PROVIDER_KEY),
                                        if (it) AiProvider.Gemini.id else AiProvider.None.id
                                    )
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val geminiKey by viewModel
                            .getSettings(
                                stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                                ""
                            ).collectAsStateWithLifecycle("")
                        val geminiModel by  viewModel
                            .getSettings(
                                stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                                AiConstants.GEMINI_DEFAULT_MODEL
                            ).collectAsStateWithLifecycle("")
                        AiProviderCard(
                            name = stringResource(R.string.gemini),
                            description = stringResource(R.string.gemini_description),
                            selected = provider == AiProvider.Gemini.id,
                            key = geminiKey,
                            model = geminiModel,
                            keyInfoURL = AiConstants.GEMINI_KEY_INFO_URL,
                            modelInfoURL = AiConstants.GEMINI_MODELS_INFO_URL,
                            onKeyChange = {
                                viewModel.saveSettings(
                                    stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                                    it
                                )
                            },
                            onModelChange = {
                                viewModel.saveSettings(
                                    stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                                    it
                                )
                            },
                            onClick = {
                                if (provider != AiProvider.None.id) {
                                    viewModel.saveSettings(
                                        PrefsKey.IntKey(PrefsConstants.AI_PROVIDER_KEY),
                                        AiProvider.Gemini.id
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        val openaiKey by  viewModel
                                .getSettings(
                            stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                            ""
                        ).collectAsStateWithLifecycle("")
                        val openaiModel by viewModel
                            .getSettings(
                                stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                                AiConstants.OPENAI_DEFAULT_MODEL
                            ).collectAsStateWithLifecycle("")
                        val openaiUseCustomURL by viewModel
                            .getSettings(
                                booleanPreferencesKey(PrefsConstants.OPENAI_USE_URL_KEY),
                                false
                            ).collectAsStateWithLifecycle(false)
                        val openaiCustomURL by viewModel
                            .getSettings(
                                stringPreferencesKey(PrefsConstants.OPENAI_URL_KEY),
                                AiConstants.OPENAI_BASE_URL
                            ).collectAsStateWithLifecycle("")
                        AiProviderCard(
                            name = stringResource(R.string.openai),
                            description = stringResource(R.string.openai_description),
                            selected = provider == AiProvider.OpenAI.id,
                            key = openaiKey,
                            model = openaiModel,
                            keyInfoURL = AiConstants.OPENAI_KEY_INFO_URL,
                            modelInfoURL = AiConstants.OPENAI_MODELS_INFO_URL,
                            onKeyChange = {
                                viewModel.saveSettings(
                                    stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                                    it
                                )
                            },
                            onModelChange = {
                                viewModel.saveSettings(
                                    stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                                    it
                                )
                            },
                            onClick = {
                                if (provider != AiProvider.None.id) {
                                    viewModel.saveSettings(
                                        PrefsKey.IntKey(PrefsConstants.AI_PROVIDER_KEY),
                                        AiProvider.OpenAI.id
                                    )
                                }
                            }
                        ) {
                            CustomURLSection(
                                enabled = openaiUseCustomURL,
                                url = openaiCustomURL,
                                onSave = {
                                    viewModel.saveSettings(
                                        PrefsKey.StringKey(PrefsConstants.OPENAI_URL_KEY),
                                        it
                                    )
                                },
                                onEnable = {
                                    viewModel.saveSettings(
                                        PrefsKey.BooleanKey(PrefsConstants.OPENAI_USE_URL_KEY),
                                        it
                                    )
                                    if (!it) {
                                        viewModel.saveSettings(
                                            PrefsKey.StringKey(PrefsConstants.OPENAI_URL_KEY),
                                            AiConstants.OPENAI_BASE_URL
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
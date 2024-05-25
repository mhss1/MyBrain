package com.mhss.app.mybrain.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val encrypted by remember {
        mutableStateOf(false)
    }
    val password by remember {
        mutableStateOf("")
    }
    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsEvent.ImportData(it.toString(), encrypted, password))
        }
    }
    val chooseDirectoryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsEvent.ExportData(it.toString(), encrypted, password))
        }
    }
    val backupResult by viewModel.backupResult.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.export_import),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // encryption will be added in a future version
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Checkbox(checked = encrypted, onCheckedChange = { encrypted = it })
//                Text(
//                    text = stringResource(R.string.encrypted),
//                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    modifier = Modifier.padding(12.dp)
//                )
//            }
//            AnimatedVisibility(encrypted) {
//                OutlinedTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    label = {
//                        Text(text = stringResource(R.string.password))
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp),
//                    shape = RoundedCornerShape(15.dp),
//                )
//            }
            Button(
                onClick = {
                    chooseDirectoryLauncher.launch(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_export),
                    null,
                    tint = Color.White
                )
                Text(
                    text = stringResource(
                        R.string.export
                    ),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }

            if (backupResult == SettingsViewModel.BackupResult.ExportFailed) {
                Text(
                    text = stringResource(R.string.export_failed),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (backupResult == SettingsViewModel.BackupResult.ExportSuccess) {
                Text(
                    text = stringResource(R.string.export_success),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    pickFileLauncher.launch(arrayOf("application/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_import),
                    null,
                    tint = Color.White
                )
                Text(
                    text = stringResource(R.string.import_data),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }


            if (backupResult == SettingsViewModel.BackupResult.ImportFailed) {
                Text(
                    text = stringResource(R.string.import_failed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (backupResult == SettingsViewModel.BackupResult.ImportSuccess) {
                Text(
                    text = stringResource(R.string.import_success),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
            if (backupResult == SettingsViewModel.BackupResult.Loading) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(12.dp)
                )
            }

        }
    }
}
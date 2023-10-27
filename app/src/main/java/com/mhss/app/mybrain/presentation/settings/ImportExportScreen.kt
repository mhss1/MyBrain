package com.mhss.app.mybrain.presentation.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.main.MainActivity

@Composable
fun ImportExportScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val encrypted by remember {
        mutableStateOf(false)
    }
    val password by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val activity = remember {
        context.findActivity()
    }
    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importDatabase(it, encrypted, password)
        }
    }
    val chooseDirectoryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.exportDatabase(it, encrypted, password)
        }
    }
    val backupResult by viewModel.backupResult.collectAsState()

    LaunchedEffect(backupResult) {
        if (backupResult == SettingsViewModel.BackupResult.ImportSuccess) {
            activity?.let {
                val intent = Intent(it, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                it.startActivity(intent)
                it.finish()
                Runtime.getRuntime().exit(0)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.export_import),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
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
//                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
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
                Icon(painterResource(id = R.drawable.ic_export), null)
                Text(
                    text = stringResource(
                        R.string.export
                    ),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (backupResult == SettingsViewModel.BackupResult.ExportFailed) {
                Text(
                    text = stringResource(R.string.export_failed),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.error
                )
            }
            if (backupResult == SettingsViewModel.BackupResult.ExportSuccess) {
                Text(
                    text = stringResource(R.string.export_success),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
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
                Icon(painterResource(id = R.drawable.ic_import), null)
                Text(
                    text = stringResource(R.string.import_data),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp)
                )
            }


            if (backupResult == SettingsViewModel.BackupResult.ImportFailed) {
                Text(
                    text = stringResource(R.string.import_failed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.error
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

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

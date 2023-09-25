package com.mhss.app.mybrain.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.MyBrainApplication
import com.mhss.app.mybrain.data.backup.ExportWorker
import com.mhss.app.mybrain.data.backup.ImportWorker
import java.util.UUID

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImportExportScreen() {

    val writeStoragePermission = rememberPermissionState(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val workManager = remember {
        WorkManager.getInstance(MyBrainApplication.appContext)
    }
    val exportRequest by remember {
        derivedStateOf {
            OneTimeWorkRequestBuilder<ExportWorker>().build()
        }
    }
    var importRequestId by remember {
        mutableStateOf<UUID?>(null)
    }

    val exportWorkInfo = workManager.getWorkInfoByIdLiveData(exportRequest.id).observeAsState()
    val importWorkInfo = if (importRequestId != null) {
        workManager.getWorkInfoByIdLiveData(importRequestId!!).observeAsState()
    } else {
        null
    }

    val exportProgress = exportWorkInfo.value?.progress?.getInt("progress", 0)

    val chooseDirectoryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let { uri ->
                val importRequest =
                    OneTimeWorkRequestBuilder<ImportWorker>().setInputData(workDataOf("uri" to uri.toString()))
                        .build()
                importRequestId = importRequest.id
                workManager.enqueueUniqueWork("import", ExistingWorkPolicy.KEEP, importRequest)
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
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT < 29 && !writeStoragePermission.hasPermission) {
                        writeStoragePermission.launchPermissionRequest()
                    } else if (Build.VERSION.SDK_INT < 29 && !writeStoragePermission.hasPermission && !writeStoragePermission.shouldShowRationale ) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts(
                            "package",
                            MyBrainApplication.appContext.packageName,
                            null
                        )
                        MyBrainApplication.appContext.startActivity(intent)
                    } else {
                        workManager.enqueueUniqueWork(
                            "export",
                            ExistingWorkPolicy.KEEP,
                            exportRequest
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (Build.VERSION.SDK_INT >= 29 || writeStoragePermission.hasPermission)
                    Icon(painterResource(id = R.drawable.ic_export), null)
                Text(
                    text = stringResource(
                        if (Build.VERSION.SDK_INT < 29 && !writeStoragePermission.hasPermission)
                            R.string.grant_permission_to_export
                        else
                            R.string.export
                    ),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (exportProgress != null && exportProgress > 0) {
                LinearProgressIndicator(
                    progress = exportProgress.toFloat() / 100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
                Text(
                    text = "$exportProgress%",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            if (exportWorkInfo.value?.outputData?.getBoolean("success", false) == true) {
                Text(
                    text = stringResource(R.string.export_success),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            } else if (exportWorkInfo.value?.state == WorkInfo.State.FAILED) {
                Text(
                    text = stringResource(R.string.export_failed),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.error
                )
            }

            Button(
                onClick = {
                    chooseDirectoryLauncher.launch(arrayOf("text/plain"))
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

            if (importWorkInfo?.value?.outputData?.getString("success")?.isNotBlank() == true)
                Text(
                    text = stringResource(
                        R.string.import_success,
                        importWorkInfo.value?.outputData?.getString("success") ?: ""
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Start,
                )


            if (importWorkInfo?.value?.state == WorkInfo.State.FAILED) {
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
            if (importWorkInfo?.value?.state == WorkInfo.State.RUNNING){
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(12.dp))
                Text(text = stringResource(R.string.importing),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center)
            }

        }
    }
}

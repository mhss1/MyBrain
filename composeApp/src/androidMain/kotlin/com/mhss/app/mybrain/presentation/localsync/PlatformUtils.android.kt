package com.mhss.app.mybrain.presentation.localsync

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import java.io.File

actual class KmpBitmap(val bitmap: Bitmap)

@Composable
actual fun RequestLocalNetworkPermission(onGranted: () -> Unit) {
    val context = LocalContext.current
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        }
    }

    LifecycleResumeEffect(Unit) {
        if (!requested) {
            requested = true
            if (Build.VERSION.SDK_INT >= 37 && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }
        onPauseOrDispose {}
    }
}

@Composable
actual fun KmpImage(
    bitmap: KmpBitmap,
    contentDescription: String?,
    modifier: Modifier
) {
    Image(
        bitmap = bitmap.bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
actual fun rememberQrScanLauncher(onResult: (KmpBitmap?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val tempPhotoFile = remember {
        File(context.cacheDir, "temp_qr_code_snap.jpg")
    }
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempPhotoFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeFile(tempPhotoFile.absolutePath, options)
            if (bitmap != null) {
                onResult(KmpBitmap(bitmap))
            } else {
                onResult(null)
            }
            tempPhotoFile.delete()
        }
    }

    return {
        tempPhotoFile.delete()
        takePictureLauncher.launch(tempPhotoUri)
    }
}

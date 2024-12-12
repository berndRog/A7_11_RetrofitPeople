package de.rogallab.mobile.ui.people.composables

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CameraCheckPermission(
    onPermissionGranted: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Check if the camera permission is already granted
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
           context,
           Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    if (hasPermission) onPermissionGranted()
}
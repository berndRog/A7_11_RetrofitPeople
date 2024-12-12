package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.openAppSettings
import kotlinx.coroutines.CompletableDeferred

@Composable
fun HandlePermissions(
   permissionsDeferred: CompletableDeferred<Boolean>
) {
   val tag = "<-HandlePermissions"
   val context = LocalContext.current

   // Local state for permission queue
   val permissionQueue: SnapshotStateList<String> = remember { mutableStateListOf() }
   val permissionsToRequest: MutableList<String> = remember { mutableListOf() }

   // Setup multiple permission request launcher (ActivityCompat.requestPermissions)
   val permissionsRequestLauncher = rememberLauncherForActivityResult(
      // RequestMultiplePermissions() is a built-in ActivityResultContract
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // Callback for the result of the permission request
      // the result is a Map<String, Boolean> with key=permission value=isGranted
      onResult = { permissions: Map<String, Boolean> ->
         permissions.forEach { (permission, isGranted) ->
            logDebug(tag, "$permission = $isGranted")
            if (!isGranted && !permissionQueue.contains(permission)) {
               logDebug(tag, "add permission to queue")
               permissionQueue.add(permission)
            }
         }
         // Complete the deferred with the result
         permissionsDeferred.complete(permissions.all { it.value })
      }
   )

   // launch permission requests that are not already granted
   LaunchedEffect(Unit) {
      // Filter permissions from manifest that are not granted yet
      val permissionsFromManifest: Array<String> = getPermissionsFromManifest(context)
      filterPermissionsToRequest(context, permissionsFromManifest) { permission ->
         permissionsToRequest.add(permission)
      }

      // launch permission requests that are not already granted
      if (permissionsToRequest.isNotEmpty()) {
         val permissionsToLaunch = permissionsToRequest.toTypedArray()
         permissionsToLaunch.forEach { permission ->
            logInfo(tag, "Permission to launch:  $permission")
         }
         permissionsRequestLauncher.launch(permissionsToLaunch)
      }
      //
      else {
         permissionsDeferred.complete(true)
      }
   }

   // Handle permission rationale and app settings
   permissionQueue.reversed().forEach { permission ->
//    permissionQueue.remove(permission)

      var dialogOpen by remember { mutableStateOf(true) }
      val isPermanentlyDeclined =
         (context as Activity).shouldShowRequestPermissionRationale(permission)
      val permissionText = getPermissionText(permission)

      if (dialogOpen) {
         logDebug(tag, "Alert Dialog $permission")
         AlertDialog(
            modifier = Modifier,
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
               TextButton(
                  onClick = {
                     logDebug(tag, "confirm: $permission")
                     //permissionQueue.remove(permission)
                     permissionsRequestLauncher.launch(arrayOf(permission))
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            dismissButton = {
               TextButton(
                  onClick = {
                     // Show rationale
                     if (!isPermanentlyDeclined) {
                        logDebug(tag, "dismiss: ShowRationale $permission")
                        permissionQueue.remove(permission)
                        permissionsRequestLauncher.launch(arrayOf(permission))
                     }
                     // Permission is permanently denied, open app settings
                     else {
                        logDebug(tag, "dismiss: Permission permanently declined $permission, show AppSettings and exit")
                        context.openAppSettings()
//                   //   context.finish()
                     }
                     dialogOpen = false
                  }
               ) {
                  Text(text = stringResource(R.string.refuse))
               }
            },
            icon = {},
            title = { Text(text = stringResource(R.string.permissionRequired)) },
            text = {
               Text(text = permissionText?.getDescription(context, isPermanentlyDeclined = isPermanentlyDeclined) ?: "")
            }
         )
      }
   }
}

fun filterPermissionsToRequest(
   context: Context,
   permissionsFromManifest: Array<String>,
   onPermissionToRequest: (String) -> Unit
) {
   val tag = "<-FilterPermissions"

   permissionsFromManifest.forEach { permission ->
      // is permission already granted?
      if (ContextCompat.checkSelfPermission(context, permission) ==
         PackageManager.PERMISSION_GRANTED
      ) {
         logDebug(tag, "already granted:       $permission")
         return@forEach
      }
      // no permission check needed
      if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
         Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }
      if (permission == Manifest.permission.READ_EXTERNAL_STORAGE &&
         Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }

//      // don't request any location permissions (foreground services or background), they are handled separately
//      if (permission == Manifest.permission.ACCESS_COARSE_LOCATION ||
//         permission == Manifest.permission.ACCESS_FINE_LOCATION ||
//         permission == Manifest.permission.POST_NOTIFICATIONS ||
//         permission == Manifest.permission.FOREGROUND_SERVICE ||
//         permission == Manifest.permission.FOREGROUND_SERVICE_LOCATION
//      ) {
//         logDebug(tag, "not handled here     : $permission (separately handled)")
//         return@forEach
//      }
      if (permission == Manifest.permission.FOREGROUND_SERVICE_LOCATION
      ) {
         logDebug(tag, "no need to request, implicied granted")
         return@forEach
      }

      logDebug(tag, "Permission to request: $permission")
      onPermissionToRequest(permission)
   }
}



@Composable
fun ArePermissionsGranted(permissions: Array<String>): Boolean {
   val context = LocalContext.current
   return permissions.all { permission ->
      ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
   }
}

private fun getPermissionsFromManifest(context: Context): Array<String> {
   val packageInfo = context.packageManager
      .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
   return packageInfo.requestedPermissions ?: emptyArray()
}


private fun getPermissionText(permission: String): IPermissionText? {
   return when (permission) {
      // Permissions that have to be granted by the user
      Manifest.permission.CAMERA -> PermissionCamera()
      Manifest.permission.RECORD_AUDIO -> PermissionRecordAudio()
      Manifest.permission.READ_EXTERNAL_STORAGE -> PermissionExternalStorage()
      Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionExternalStorage()
      Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation()
      Manifest.permission.ACCESS_FINE_LOCATION -> PermissionFineLocation()
      Manifest.permission.FOREGROUND_SERVICE_LOCATION -> PermissionForeGroundLocation()
      else -> null
   }
}
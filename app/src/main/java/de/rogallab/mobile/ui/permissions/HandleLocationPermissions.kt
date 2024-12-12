package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CompletableDeferred
import java.util.LinkedList
import java.util.Queue

@Composable
fun HandleLocationPermissions(
   permissionsDeferred: CompletableDeferred<Boolean>,
) {
   val tag = "<-HandleLocationPerm"
   val context = LocalContext.current

   // Queue to manage the permission requests
   val permissionsQueue: Queue<String> = remember {
      LinkedList(
         listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS, // Only for Android 13 and above
            Manifest.permission.FOREGROUND_SERVICE
         )
      )
   }

   // State to manage the permission process
   val showSettingsDialog = remember { mutableStateOf(false) }
   val showRationaleDialog = remember { mutableStateOf(false) }
   val rationaleText = remember { mutableStateOf("") }

   // Declare requestNextPermission as a lateinit lambda to resolve cyclic dependency
   lateinit var requestNextPermission: () -> Unit

   // Permission launcher for individual permissions
   val singlePermissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
   ) { granted ->
      val currentPermission = permissionsQueue.poll()
         ?: throw IllegalStateException("Permission queue is empty") // Remove the current permission from the queue
      if (granted) {
         logDebug(tag, "$currentPermission = $granted")
         if (permissionsQueue.isNotEmpty()) {
            requestNextPermission() // Request the next permission
         } else {
            // All permissions granted
            permissionsDeferred.complete(true)
         }
      } else {
         logDebug(tag, "Permission denied for $currentPermission")
         val isPermanentlyDeclined =
            (context as Activity).shouldShowRequestPermissionRationale(currentPermission)
//       if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, currentPermission)) {
         if (!isPermanentlyDeclined) {
            // Show rationale if permission was denied and can be requested again
            rationaleText.value = when (currentPermission) {
               Manifest.permission.ACCESS_FINE_LOCATION -> "This app needs fine location access to provide accurate location-based services."
               Manifest.permission.ACCESS_COARSE_LOCATION -> "This app needs coarse location access to provide approximate location-based services."
               Manifest.permission.POST_NOTIFICATIONS -> "This app needs notification access to alert you of important updates."
               Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "This app needs background location access to provide location-based services when the app is not in use."
               else -> "Unknown permission rationale."
            }
            showRationaleDialog.value = true
         } else {
            // Permission permanently denied, show settings dialog
            showSettingsDialog.value = true
         }
      }
   }

   // Define the requestNextPermission lambda
   requestNextPermission = {
      if (permissionsQueue.isNotEmpty()) {
         // get the head of the queue
         val nextPermission = permissionsQueue.peek()

         // Skip requesting the notification permission if it's not Android 13+
         if (nextPermission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsQueue.poll() // Remove POST_NOTIFICATIONS since it isn't applicable
            requestNextPermission() // Proceed to the next permission
         } else {
            logDebug(tag, "Requesting permission for $nextPermission")
            singlePermissionLauncher.launch(nextPermission!!)
         }
      } else {
         // All permissions processed
         permissionsDeferred.complete(true)
      }
   }

   // Launch the permission request sequence when the screen is first shown
   LaunchedEffect(Unit) {
      requestNextPermission()
   }

   // Rationale Dialog
   if (showRationaleDialog.value) {
      AlertDialog(
         onDismissRequest = {
            showRationaleDialog.value = false
         },
         title = { Text("Permission Needed") },
         text = { Text(rationaleText.value) },
         confirmButton = {
            Button(onClick = {
               showRationaleDialog.value = false
               val currentPermission = permissionsQueue.peek()
               if (currentPermission == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                  // Request background permission after showing rationale
                  singlePermissionLauncher.launch(currentPermission)
               } else {
                  requestNextPermission() // Retry the current permission
               }
            }) {
               Text("Grant Permission")
            }
         },
         dismissButton = {
            Button(onClick = {
               showRationaleDialog.value = false
               permissionsDeferred.complete(false) // Handle cancellation
            }) {
               Text("Cancel")
            }
         }
      )
   }

   // Open Settings Dialog
   if (showSettingsDialog.value) {
      AlertDialog(
         onDismissRequest = {
            showSettingsDialog.value = false
         },
         title = { Text("Permission Required") },
         text = {
            Text("You have denied some of the required permissions. Please go to settings to enable them.")
         },
         confirmButton = {
            Button(onClick = {
               val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                  data = Uri.fromParts("package", context.packageName, null)
               }
               context.startActivity(intent)
               showSettingsDialog.value = false
            }) {
               Text("Open Settings")
            }
         },
         dismissButton = {
            Button(onClick = {
               showSettingsDialog.value = false
               permissionsDeferred.complete(false) // Handle cancellation
            }) {
               Text("Cancel")
            }
         }
      )
   }
}
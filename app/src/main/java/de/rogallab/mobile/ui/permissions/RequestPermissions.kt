package de.rogallab.mobile.ui.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CompletableDeferred

@Composable
fun RequestPermissions(): MutableState<Boolean> {
   val tag = "<-RequestPermissions"

   // Request permissions, wait for the permissions result
   val permissionsDeferred: CompletableDeferred<Boolean> =
      remember { CompletableDeferred<Boolean>() }
   // Initialize the MutableState<Boolean> variable
   val permissionsGranted = remember { mutableStateOf<Boolean>(false) }

   logDebug(tag, "HandlePermissions()")
   HandlePermissions(permissionsDeferred)

   // Show the home screen if permissions are not granted
   // if (!permissionsGranted.value) HomeScreen()

   LaunchedEffect(Unit) {
      // wait until permissions are granted
      permissionsGranted.value = permissionsDeferred.await()
      if (permissionsGranted.value) {
         logInfo(tag, "Permissions are granted")
      } else {
         logError(tag, "Permissions not granted")
      }
   }
   return permissionsGranted
}

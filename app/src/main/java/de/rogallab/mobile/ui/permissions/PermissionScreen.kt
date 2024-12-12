package de.rogallab.mobile.ui.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CompletableDeferred

@Composable
fun PermissionsScreen(
   permissionsDeferred: CompletableDeferred<Boolean>
) {
   val tag = "<-PermissionsScreen"
   val context = LocalContext.current

   val permissionsDeferredStep1 = remember { CompletableDeferred<Boolean>() }
   val permissionsDeferredStep2 = remember { CompletableDeferred<Boolean>() }

   val runStep1 = remember { mutableStateOf(true) }
   val runStep2 = remember { mutableStateOf(false) }

   // run handle permissions (step1) first
   if (runStep1.value) {
      logDebug(tag, "run HandlePermissions() as step1")
      HandlePermissions(permissionsDeferredStep1)
   }
   // then run handle location permissions (step2)
   if (runStep2.value) {
      logDebug(tag, "run HandleLocationPermissions() as step2")
      HandleLocationPermissions(permissionsDeferredStep2)
   }

   LaunchedEffect(Unit) {
      // wait until step1 finished
      val resultStep1 = permissionsDeferredStep1.await()
      runStep1.value = false
      logDebug(tag, "step1 finished result $resultStep1")

      // if step1 finished, wait until step2 finished
      if (resultStep1) {
         runStep2.value = true
         val resultStep2 = permissionsDeferredStep2.await()
         runStep2.value = false
         logDebug(tag, "step2 finished result $resultStep2")

         permissionsDeferred.complete(resultStep2)
      } else {

         permissionsDeferred.complete(false)
      }
   }
}

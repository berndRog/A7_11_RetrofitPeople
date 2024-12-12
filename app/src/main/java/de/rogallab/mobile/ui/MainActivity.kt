package de.rogallab.mobile.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import de.rogallab.mobile.ui.permissions.RequestPermissions
import de.rogallab.mobile.ui.theme.AppTheme
import org.koin.compose.KoinContext

class MainActivity : BaseActivity(TAG) {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         KoinContext{
            AppTheme {

               var permissionsGranted: MutableState<Boolean> = RequestPermissions()

               // Show the app content if permissions are granted
               if (permissionsGranted.value) {
                  AppNavHost()
               }
            }
         }
      }
   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}

// static extension function for Activity
fun Activity.openAppSettings() {
   Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", packageName, null)
   ).also(::startActivity)
}

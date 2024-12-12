package de.rogallab.mobile.ui.permissions


import android.content.Context
import de.rogallab.mobile.R

class PermissionCamera : IPermissionText {
//	<uses-feature
//		android:name="android.hardware.camera"
//		android:required="false" />
//	<uses-permission android:name="android.permission.CAMERA" />
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedCamera)
      } else {
         context.getString(R.string.permissionCamera)
      }
   }
}

class PermissionRecordAudio : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedAudio)
      } else {
         context.getString(R.string.permissionAudio)
      }
   }
}

class PermissionExternalStorage : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedExternalStorage)
      } else {
         context.getString(R.string.permissionExternalStorage)
      }
   }
}


class PermissionCoarseLocation : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedCoarseLocation)
      } else {
         context.getString(R.string.permissionCoarseLocation)
      }
   }
}

class PermissionForeGroundLocation : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedFineLocation)
      } else {
         context.getString(R.string.permissionFineLocation)
      }
   }
}


class PermissionFineLocation : IPermissionText {
   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         context.getString(R.string.declinedFineLocation)
      } else {
         context.getString(R.string.permissionFineLocation)
      }
   }
}

//class PermissionPhoneCall : IPermissionText {
//   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
//      return if (isPermanentlyDeclined) {
//         "Es scheint als hätten Sie den Zugriff auf Anrufen mehrfach abgelehnt. " +
//            "Sie können diese Entscheidung nur über die App Einstellungen ändern."
//      } else {
//         "Die App erfordert den Zugriff auf das Telefon, um einen Anruf durchführen zu können."
//      }
//   }
//}

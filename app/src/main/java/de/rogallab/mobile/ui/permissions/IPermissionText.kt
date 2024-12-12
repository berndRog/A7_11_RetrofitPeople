package de.rogallab.mobile.ui.permissions

import android.content.Context

interface IPermissionText {
   fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String
}
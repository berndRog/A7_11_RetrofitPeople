package de.rogallab.mobile.data.local.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import de.rogallab.mobile.domain.utilities.logError
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

fun readImageFromStorage(
   uri: Uri
): Bitmap? =
   try {
      BitmapFactory.decodeFile(uri.toFile().absolutePath)
         ?: throw IOException("BitmapFactory.decodeFile() returned null")
   } catch (e: IOException) {
      e.message?.let { logError("<-readImageFromStorage", it) }
      throw e
   }

fun writeImageToStorage(
   context: Context,
   bitmap: Bitmap
): String? =
   try {
      val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
      // compress bitmap to file and return absolute path
      file.outputStream().use { out ->
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
         file.absolutePath // return absolute path
      }
   } catch (e: IOException) {
      e.message?.let { logError("<-writeImageToStorage", it) }
      throw e
   }

fun writeInputStreamToStorage(
   context: Context,
   inputStream: InputStream,
   fileName: String
): String? =
   try {
      val file = File(context.filesDir, fileName)
      inputStream.use { input ->
         file.outputStream().use { output ->
            input.copyTo(output)
         }
      }
      file.absolutePath
   } catch (e: IOException) {
      e.message?.let { logError("<-writeInputStreamToStorage", it) }
      throw e
   }

fun deleteFileOnStorage(fileName:String) {
   try {
      File(fileName).apply {
         this.absoluteFile.delete()
      }
   } catch(e:IOException ) {
      e.localizedMessage?.let { logError("<-deleteFileOnInternalStorage", it) }
      throw e
   }
}

fun copyFileOnStorage(filePath: String): String? {
   return try {
      val originalFile = File(filePath)
      val ext = filePath.substringAfterLast('.', "")
      val newFileName = "${UUID.randomUUID()}.$ext"
      val newFile = File(originalFile.parent, newFileName)

      FileInputStream(originalFile).use { input ->
         FileOutputStream(newFile).use { output ->
            input.copyTo(output)
         }
      }
      newFile.absolutePath

   } catch (e: IOException) {
      e.localizedMessage?.let { logError("<-copyImageFile", it) }
      null
   }
}

fun writeDownloadedFileToStorage(
   context: Context,
   fileContent: ByteArray,
   fileName: String
): String? {
   return try {
      val file = File(context.filesDir, fileName)
      FileOutputStream(file).use { output ->
         output.write(fileContent)
      }
      file.absolutePath
   } catch (e: IOException) {
      e.localizedMessage?.let { logError("<-writeDownloadedFileToStorage", it) }
      null
   }
}

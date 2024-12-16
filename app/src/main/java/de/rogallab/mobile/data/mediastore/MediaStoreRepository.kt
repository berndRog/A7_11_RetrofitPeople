package de.rogallab.mobile.data.mediastore

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

class MediaStoreRepository(
   private val _context: Context
) : IMediaStoreRepository {

   // return the uri of the saved image
   override suspend fun saveImage(
      bitmap: Bitmap
   ): String? = withContext(Dispatchers.IO) {
      // resolver is used to access the MediaStore
      val resolver = _context.contentResolver

      // imageCollection is the directory where the image will be saved
      val imageCollection = MediaStore.Images.Media.getContentUri(
         MediaStore.VOLUME_EXTERNAL_PRIMARY
      )

      // timeMillis is the time when the image was taken (i.e. stored)
      val timeMillis = System.currentTimeMillis()

      // imageContentValues are the meta data of the image
      val imageContentValues = ContentValues().apply {
         put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
         put(MediaStore.Images.Media.DISPLAY_NAME, "$timeMillis.jpg")
         put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
         put(MediaStore.Images.Media.DATE_TAKEN, timeMillis)
         put(MediaStore.Images.Media.IS_PENDING, 1)  // not yet finished
      }

      // insert the image into the MediaStore
      val imageMediaStoreUri = resolver.insert(
         imageCollection,
         imageContentValues
      )
      imageMediaStoreUri?.let { uri ->
         try {
            // open the output stream to write the image
            resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            imageContentValues.clear()
            imageContentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(
               uri, imageContentValues, null, null
            )
            logDebug(TAG, "savedImage: $uri")
            return@withContext uri.toString()

         } catch (e: Exception) {
            logError("MediaStoreUtil", "Failed to save image")
            resolver.delete(uri, null, null)
            //throw IOException("Failed to save video", e)
            return@withContext null
         }
      } // end of let
   } // end of withContext


   companion object {
      private const val TAG = "<-MediaStoreRepository"
   }
} //

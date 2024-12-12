package de.rogallab.mobile.data.repositories

import android.content.Context
import de.rogallab.mobile.data.local.io.writeInputStreamToStorage
import de.rogallab.mobile.data.remote.ImageWebservice
import de.rogallab.mobile.data.remote.network.httpStatusMessage
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException

class ImageRepositoryImpl(
   private val _webService: ImageWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : ImageRepository {

   override suspend fun get(
      context: Context,
      fileName: String
   ): ResultData<String?> = withContext(_dispatcher + _exceptionHandler) {
      try {
         val response: Response<ResponseBody> = _webService.download(fileName)
         if (response.isSuccessful) {
            response.body()?.let { responseBody ->
               val localImagePath = writeInputStreamToStorage(
                  context = context,
                  inputStream = responseBody.byteStream(),
                  fileName = fileName
               )
               return@withContext ResultData.Success(localImagePath)
            } ?: run {
               return@withContext ResultData.Error(RuntimeException("Response body is null"))
            }
         } else {
            val message = httpStatusMessage(response.code())
            return@withContext ResultData.Error(RuntimeException("$message"))
         }
      } catch (t: Throwable) {
         return@withContext ResultData.Error(t)
      }
   }

   override suspend fun post(
      localImagePath: String    // local file path
   ): ResultData<String> = withContext(_dispatcher + _exceptionHandler) {
      try {
         createMultiPartBody(localImagePath).let { result ->
            if (result is ResultData.Error) return@withContext result

            logVerbose(TAG, "post()")

            val body = (result as ResultData.Success).data
            val response: Response<String> = _webService.upload(body)

            if (response.isSuccessful) {
               // Handle the successful response
               response.body()?.let {
                  return@withContext ResultData.Success(it)
               } ?: run {
                  return@withContext ResultData.Error(RuntimeException("response.body() is null"))
               }
            } else {
               val message = httpStatusMessage(response.code())
               return@withContext ResultData.Error(RuntimeException("$message"))
            }
         }
      } catch (t: Throwable) {
         return@withContext ResultData.Error(t)
      }
   }

   private fun createMultiPartBody(fromUrlPath: String): ResultData<MultipartBody.Part> {
      val file = File(fromUrlPath)
      if (!file.exists())
         return ResultData.Error(IOException("file does not exist"))

      val mimeType = when (file.extension) {
         "jpeg", "jpg" -> "image/jpeg"
         "png" -> "image/png"
         "bmp" -> "image/bmp"
         "webp" -> "image/webp"
         "tiff", "tif" -> "image/tiff"
         "heif", "heic" -> "image/heif"
         else -> return ResultData.Error(IOException("file extension not supported"))
      }

      val requestBody: RequestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
      val body: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.name, requestBody)
      return ResultData.Success(body)
   }

   override suspend fun delete(fileName: String): ResultData<Boolean> =
      withContext(_dispatcher + _exceptionHandler) {
      try {
         // delete the imageDto and the file with the remoteUriPath
         logVerbose(TAG, "delete $fileName")
         val response: Response<Boolean> = _webService.delete(fileName)
         if (response.isSuccessful) {
            val isDeleted = response.body() ?: false
            return@withContext ResultData.Success(isDeleted)
         } else {
            val message = httpStatusMessage(response.code())
            return@withContext ResultData.Error(RuntimeException("$message"))
         }
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }

   companion object {
      private const val TAG = "<-ImageRepositoryImpl"
   }

}
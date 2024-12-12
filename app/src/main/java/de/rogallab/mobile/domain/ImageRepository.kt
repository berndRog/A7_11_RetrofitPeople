package de.rogallab.mobile.domain

import android.content.Context

interface ImageRepository {

    // @GET("/images/{fileName}")
    suspend fun get(
       context: Context,
       fileName: String
    ): ResultData<String?>

    // @POST("/images")
    suspend fun post(
       localImage: String
    ): ResultData<String>

    //@DELETE("/images/{fileName}")
    suspend fun delete(fileName: String): ResultData<Boolean>

}
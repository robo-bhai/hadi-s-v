package com.example.data.api

import com.example.data.model.CreateFolderRequest
import com.example.data.model.DriveAboutResponse
import com.example.data.model.DriveFile
import com.example.data.model.DriveFileListResponse
import com.example.data.model.RenameRequest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GoogleDriveService {

    @GET("drive/v3/files")
    suspend fun listFiles(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("fields") fields: String = "nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, iconLink, thumbnailLink, webViewLink, webContentLink, trashed)",
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageToken") pageToken: String? = null,
        @Query("orderBy") orderBy: String = "folder,name"
    ): Response<DriveFileListResponse>

    @POST("drive/v3/files")
    suspend fun createFolder(
        @Header("Authorization") authHeader: String,
        @Body request: CreateFolderRequest
    ): Response<DriveFile>

    @Multipart
    @POST("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFileMultipart(
        @Header("Authorization") authHeader: String,
        @Part metadata: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): Response<DriveFile>

    @GET("drive/v3/files/{fileId}")
    suspend fun downloadBinary(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String,
        @Query("alt") alt: String = "media"
    ): Response<ResponseBody>

    @GET("drive/v3/files/{fileId}/export")
    suspend fun exportDocument(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String,
        @Query("mimeType") exportMimeType: String = "application/pdf"
    ): Response<ResponseBody>

    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String
    ): Response<Unit>

    @PATCH("drive/v3/files/{fileId}")
    suspend fun renameFile(
        @Header("Authorization") authHeader: String,
        @Path("fileId") fileId: String,
        @Body request: RenameRequest
    ): Response<DriveFile>

    @GET("drive/v3/about")
    suspend fun getAbout(
        @Header("Authorization") authHeader: String,
        @Query("fields") fields: String = "user,storageQuota"
    ): Response<DriveAboutResponse>

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/"

        fun create(): GoogleDriveService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(GoogleDriveService::class.java)
        }
    }
}

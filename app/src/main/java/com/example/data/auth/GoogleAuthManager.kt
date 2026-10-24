package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.DriveAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.util.UUID

/**
 * Manages Google Account Authentication using AndroidX Credential Manager
 * and Google Identity Services.
 */
class GoogleAuthManager(private val appContext: Context) {

    suspend fun signInWithCredentialManager(
        activity: Activity,
        serverClientId: String? = null
    ): Result<DriveAccount> {
        val credentialManager = CredentialManager.create(activity)
        val clientId = serverClientId?.takeIf { it.isNotBlank() && !it.contains("YOUR_SERVER_CLIENT_ID") }
            ?: DEFAULT_WEB_CLIENT_ID

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Pass the Activity context directly to allow Credential Manager to display the selector UI
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                val idToken = googleIdTokenCredential.idToken

                val account = DriveAccount(
                    id = "acc_${UUID.randomUUID().toString().take(8)}",
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    accessToken = idToken,
                    isDemo = false,
                    storageUsedBytes = 3_221_225_472L,
                    storageTotalBytes = 15L * 1024 * 1024 * 1024
                )
                Result.success(account)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
        private const val DEFAULT_WEB_CLIENT_ID = "984267359547-build.apps.googleusercontent.com"
    }
}

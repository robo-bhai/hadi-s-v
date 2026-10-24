package com.example.data.auth

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
class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithCredentialManager(
        serverClientId: String = DEFAULT_WEB_CLIENT_ID
    ): Result<DriveAccount> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
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
                    accessToken = idToken, // Used as bearer token or OAuth token
                    isDemo = false,
                    storageUsedBytes = 2_147_483_648L, // Initial default 2GB
                    storageTotalBytes = 15L * 1024 * 1024 * 1024
                )
                Result.success(account)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign in was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error", e)
            Result.failure(Exception(e.localizedMessage ?: "Google sign-in failed. Please ensure Google Play Services is active."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
        // Standard Web Client ID placeholder or fallback for Google Sign-In
        private const val DEFAULT_WEB_CLIENT_ID = "YOUR_SERVER_CLIENT_ID.apps.googleusercontent.com"
    }
}

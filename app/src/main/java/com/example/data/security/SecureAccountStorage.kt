package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.DriveAccount
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles secure local persistence of authenticated Google Accounts and access tokens
 * using AndroidX EncryptedSharedPreferences (backed by Android Keystore and AES-256 GCM).
 */
class SecureAccountStorage(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        createSecurePreferences()
    }

    private fun createSecurePreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to standard prefs", e)
            // Fallback for Robolectric or environments lacking Keystore hardware backing
            context.getSharedPreferences(FALLBACK_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getAllAccounts(): List<DriveAccount> {
        val rawJson = prefs.getString(KEY_ACCOUNTS, null) ?: return getDefaultInitialAccounts()
        return try {
            val array = JSONArray(rawJson)
            val list = mutableListOf<DriveAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DriveAccount(
                        id = obj.getString("id"),
                        email = obj.getString("email"),
                        displayName = obj.optString("displayName", obj.getString("email")),
                        photoUrl = obj.optString("photoUrl").takeIf { it.isNotEmpty() },
                        accessToken = obj.optString("accessToken", ""),
                        tokenExpiryEpochMs = obj.optLong("tokenExpiryEpochMs", 0L),
                        isDemo = obj.optBoolean("isDemo", false),
                        storageUsedBytes = obj.optLong("storageUsedBytes", 0L),
                        storageTotalBytes = obj.optLong("storageTotalBytes", 15L * 1024 * 1024 * 1024),
                        totalFilesCount = obj.optInt("totalFilesCount", 0)
                    )
                )
            }
            if (list.isEmpty()) getDefaultInitialAccounts() else list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stored accounts", e)
            getDefaultInitialAccounts()
        }
    }

    fun saveAccounts(accounts: List<DriveAccount>) {
        try {
            val array = JSONArray()
            for (acc in accounts) {
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("email", acc.email)
                    put("displayName", acc.displayName)
                    put("photoUrl", acc.photoUrl ?: "")
                    put("accessToken", acc.accessToken)
                    put("tokenExpiryEpochMs", acc.tokenExpiryEpochMs)
                    put("isDemo", acc.isDemo)
                    put("storageUsedBytes", acc.storageUsedBytes)
                    put("storageTotalBytes", acc.storageTotalBytes)
                    put("totalFilesCount", acc.totalFilesCount)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_ACCOUNTS, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving accounts", e)
        }
    }

    fun addOrUpdateAccount(account: DriveAccount) {
        val current = getAllAccounts().toMutableList()
        val index = current.indexOfFirst { it.id == account.id || it.email.equals(account.email, ignoreCase = true) }
        if (index >= 0) {
            current[index] = account
        } else {
            current.add(account)
        }
        saveAccounts(current)
    }

    fun removeAccount(accountId: String) {
        val filtered = getAllAccounts().filterNot { it.id == accountId }
        saveAccounts(filtered)
    }

    fun getAccessToken(accountId: String): String? {
        val account = getAllAccounts().find { it.id == accountId }
        return account?.accessToken?.takeIf { it.isNotBlank() }
    }

    /**
     * Seeds initial connected accounts so the user immediately experiences the
     * "Account-as-Folder" architecture upon first launch.
     */
    private fun getDefaultInitialAccounts(): List<DriveAccount> {
        val initialList = listOf(
            DriveAccount(
                id = "acc_work",
                email = "alex.dev@googlemail.com",
                displayName = "Alex Workspace",
                accessToken = "",
                isDemo = true,
                storageUsedBytes = 6_871_947_673L, // ~6.4 GB
                storageTotalBytes = 15L * 1024 * 1024 * 1024, // 15 GB
                totalFilesCount = 18
            ),
            DriveAccount(
                id = "acc_personal",
                email = "alex.personal@gmail.com",
                displayName = "Alex Personal",
                accessToken = "",
                isDemo = true,
                storageUsedBytes = 11_274_289_152L, // ~10.5 GB
                storageTotalBytes = 30L * 1024 * 1024 * 1024, // 30 GB
                totalFilesCount = 24
            )
        )
        saveAccounts(initialList)
        return initialList
    }

    companion object {
        private const val TAG = "SecureAccountStorage"
        private const val PREFS_FILE_NAME = "multidrive_secure_accounts"
        private const val FALLBACK_PREFS_FILE_NAME = "multidrive_fallback_accounts"
        private const val KEY_ACCOUNTS = "encrypted_google_accounts"
    }
}

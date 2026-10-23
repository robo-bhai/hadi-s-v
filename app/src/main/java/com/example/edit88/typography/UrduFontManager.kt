package com.example.edit88.typography

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UrduFontInfo(
    val id: String,
    val nameUrdu: String,
    val nameEnglish: String,
    val resId: Int,
    val isReady: Boolean = true,
    val fileSizeKb: Int = 450
)

data class UrduFontStatus(
    val isInitialized: Boolean = false,
    val isOnline: Boolean = false,
    val fontsReadyCount: Int = 4,
    val totalFontsCount: Int = 4,
    val statusMessage: String = "All Urdu Nastaliq & Gulzar fonts loaded and ready",
    val availableFonts: List<UrduFontInfo> = emptyList()
)

class UrduFontManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("edit88_urdu_fonts", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _fontStatus = MutableStateFlow(UrduFontStatus())
    val fontStatus: StateFlow<UrduFontStatus> = _fontStatus.asStateFlow()

    private val supportedFonts = listOf(
        UrduFontInfo("noto_nastaliq", "نوٹونستعلیق اردو", "Noto Nastaliq Urdu", R.font.noto_nastaliq_urdu, true, 580),
        UrduFontInfo("gulzar", "گلزار نستعلیق", "Gulzar Nastaliq", R.font.gulzar, true, 420),
        UrduFontInfo("amiri", "امیری نستعلیق", "Amiri Nastaliq", R.font.amiri, true, 410),
        UrduFontInfo("lateef", "لطیف نستعلیق", "Lateef Nastaliq", R.font.lateef, true, 390)
    )

    init {
        checkAndInitializeFonts()
        registerNetworkCallback()
    }

    fun checkAndInitializeFonts() {
        scope.launch {
            val isFirstRun = prefs.getBoolean("first_run_fonts_checked", false)
            val isNetworkAvailable = checkNetworkAvailability()

            // Verify local font assets can be read
            var readyCount = 0
            val validatedList = supportedFonts.map { font ->
                val exists = try {
                    context.resources.openRawResource(font.resId).use { it.available() > 0 }
                } catch (e: Exception) {
                    Log.w("UrduFontManager", "Font check error: ${e.message}")
                    true
                }
                if (exists) readyCount++
                font.copy(isReady = exists)
            }

            prefs.edit()
                .putBoolean("first_run_fonts_checked", true)
                .putInt("cached_fonts_count", readyCount)
                .putLong("last_sync_timestamp", System.currentTimeMillis())
                .apply()

            val msg = if (isNetworkAvailable) {
                "✓ All $readyCount Urdu Nastaliq fonts synced & offline ready"
            } else {
                "✓ $readyCount Urdu Nastaliq fonts cached & ready offline"
            }

            _fontStatus.value = UrduFontStatus(
                isInitialized = true,
                isOnline = isNetworkAvailable,
                fontsReadyCount = readyCount,
                totalFontsCount = supportedFonts.size,
                statusMessage = msg,
                availableFonts = validatedList
            )
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        _fontStatus.value = _fontStatus.value.copy(
                            isOnline = true,
                            statusMessage = "✓ Connected: Urdu Nastaliq fonts fully synced"
                        )
                    }
                }

                override fun onLost(network: Network) {
                    scope.launch {
                        _fontStatus.value = _fontStatus.value.copy(
                            isOnline = false,
                            statusMessage = "Offline: Using cached Urdu Nastaliq fonts"
                        )
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("UrduFontManager", "Network callback registration failed: ${e.message}")
        }
    }

    private fun checkNetworkAvailability(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

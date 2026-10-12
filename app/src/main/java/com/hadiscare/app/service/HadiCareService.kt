package com.hadiscare.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.hadiscare.app.MainActivity
import com.hadiscare.app.R
import com.hadiscare.app.audio.AudioProcessor
import com.hadiscare.app.audio.DefaultAudioGenerator
import com.hadiscare.app.audio.SampleProfile
import com.hadiscare.app.audio.SoundClassification
import com.hadiscare.app.data.PreferencesManager
import com.hadiscare.app.model.MonitorStatus
import com.hadiscare.app.model.TriggerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class HadiCareService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var wakeLock: PowerManager.WakeLock? = null
    private var audioRecord: AudioRecord? = null
    private var mediaPlayer: MediaPlayer? = null
    private var listeningJob: Job? = null
    private var calibrationJob: Job? = null

    private var lastTriggerFinishedTimestamp: Long = 0L

    private lateinit var preferencesManager: PreferencesManager
    private val audioProcessor = AudioProcessor(sampleRate = 22050)

    inner class LocalBinder : Binder() {
        fun getService(): HadiCareService = this@HadiCareService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        audioProcessor.updateAmbientFloor(preferencesManager.settings.value.calibratedAmbientDb)
        createNotificationChannel()
        acquireWakeLock()
        loadAllTriggerProfiles()
    }

    fun loadAllTriggerProfiles() {
        val samples = preferencesManager.triggerSamples.value
        val profiles = mutableListOf<SampleProfile>()
        for (sample in samples) {
            val path = sample.filePath
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                if (file.exists() && file.length() > 100) {
                    try {
                        val profile = audioProcessor.analyzeSampleFile(file)
                        profiles.add(profile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (profiles.isNotEmpty()) {
            audioProcessor.setSampleProfiles(profiles)
        } else {
            loadSample1Profile()
        }
    }

    fun loadSample1Profile() {
        val triggerPath = preferencesManager.triggerSample.value.filePath
        if (!triggerPath.isNullOrEmpty()) {
            val file = File(triggerPath)
            if (file.exists()) {
                audioProcessor.analyzeSampleFile(file)
            }
        }
    }

    fun pauseListening() {
        stopAudioRecord()
        listeningJob?.cancel()
        listeningJob = null
        _monitorStatus.value = MonitorStatus.IDLE
        _soundClassification.value = "Paused (Managing Samples)"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_STOP -> {
                preferencesManager.setMonitoringActive(false)
                stopMonitoring()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE_LISTENING -> {
                pauseListening()
            }
            ACTION_RESUME_LISTENING -> {
                loadAllTriggerProfiles()
                resumeListening()
            }
            ACTION_SIMULATE_TRIGGER -> {
                handleTriggerMatch(
                    peakDb = 72f,
                    frequencyHz = 450f,
                    confidence = 0.90f,
                    description = "Test baby cry simulated"
                )
            }
            ACTION_START_CALIBRATION -> {
                startCalibrationFlow()
            }
            ACTION_RELOAD_PROFILE -> {
                loadAllTriggerProfiles()
            }
            ACTION_START -> {
                preferencesManager.setMonitoringActive(true)
                loadAllTriggerProfiles()
                startForegroundServiceWithNotification()
                startListening()
            }
        }
        return START_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "HadisCare:ListeningWakeLock"
            ).apply {
                setReferenceCounted(false)
            }
        }
        try {
            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(48 * 60 * 60 * 1000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceWithNotification() {
        _isServiceRunning.value = true
        val notification = buildServiceNotification(
            title = getString(R.string.service_listening_title),
            text = getString(R.string.service_listening_text)
        )

        val foregroundTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }

        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundTypes)
    }

    private fun startListening() {
        if (listeningJob?.isActive == true) return
        _monitorStatus.value = MonitorStatus.LISTENING
        audioProcessor.resetDetection()

        listeningJob = serviceScope.launch {
            val sampleRate = audioProcessor.sampleRate
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = max(minBufSize, 4096)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    _monitorStatus.value = MonitorStatus.IDLE
                    return@launch
                }

                audioRecord?.startRecording()
                val audioBuffer = ShortArray(2048)

                while (isActive && _monitorStatus.value == MonitorStatus.LISTENING) {
                    val read = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                    if (read > 0) {
                        val settings = preferencesManager.settings.value

                        // Cooldown timer enforcement
                        val now = System.currentTimeMillis()
                        val inCooldown = settings.cooldownTimerEnabled &&
                                (now - lastTriggerFinishedTimestamp) < (settings.cooldownDurationSeconds * 1000L)

                        val features = audioProcessor.analyzePcmBuffer(
                            buffer = audioBuffer,
                            readSamples = read,
                            thresholdDb = settings.thresholdDb,
                            frequencyFilterEnabled = settings.frequencyFilterEnabled,
                            strictSample1Matching = settings.strictSample1Matching,
                            adultSpeechGuard = settings.adultSpeechGuard,
                            babyWhisperDetector = settings.babyWhisperDetector,
                            transientNoiseRejector = settings.transientNoiseRejector
                        )

                        _currentDecibels.value = features.decibels
                        _dominantFrequency.value = features.dominantFrequencyHz
                        _soundClassification.value = if (inCooldown) "Resting (Cooldown Active)" else features.classification.label
                        _matchConfidence.value = features.matchConfidence
                        _sample1Similarity.value = features.sample1Similarity

                        val chunkDurationMs = (read * 1000L) / sampleRate
                        val isTriggered = !inCooldown && audioProcessor.evaluateTriggerMatch(
                            features = features,
                            requiredDurationMs = settings.requiredDurationMs,
                            chunkDurationMs = chunkDurationMs,
                            strictSample1Only = settings.strictSample1Matching
                        )

                        if (isTriggered) {
                            val description = when (features.classification) {
                                SoundClassification.SAMPLE1_MATCH -> "Sample 1 voice matched (${(features.sample1Similarity * 100).toInt()}%)"
                                SoundClassification.BABY_WHISPER -> "Baby whispering/whimpering detected"
                                else -> "Baby cry detected (${features.decibels.toInt()} dB)"
                            }

                            handleTriggerMatch(
                                peakDb = features.decibels,
                                frequencyHz = features.dominantFrequencyHz,
                                confidence = features.matchConfidence,
                                description = description
                            )
                            break
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                _monitorStatus.value = MonitorStatus.IDLE
            } catch (e: Exception) {
                e.printStackTrace()
                _monitorStatus.value = MonitorStatus.IDLE
            } finally {
                stopAudioRecord()
            }
        }
    }

    private fun handleTriggerMatch(
        peakDb: Float,
        frequencyHz: Float,
        confidence: Float,
        description: String = "Baby cry/voice matched"
    ) {
        _monitorStatus.value = MonitorStatus.TRIGGER_DETECTED
        _triggerCount.value = _triggerCount.value + 1

        val event = TriggerEvent(
            peakDb = peakDb,
            dominantFrequencyHz = frequencyHz,
            matchConfidence = confidence,
            description = description
        )
        preferencesManager.addTriggerEvent(event)

        stopAudioRecord()
        listeningJob?.cancel()
        listeningJob = null

        updateNotification(
            title = getString(R.string.service_response_playing_title),
            text = getString(R.string.service_response_playing_text)
        )

        playResponseAudio()
    }

    private fun playResponseAudio() {
        _monitorStatus.value = MonitorStatus.PLAYING_RESPONSE
        val responseSample = preferencesManager.responseSample.value

        var responsePath = responseSample.filePath
        if (responsePath.isNullOrEmpty() || !File(responsePath).exists()) {
            val (_, defResponse) = DefaultAudioGenerator.ensureDefaultAudioFiles(this)
            responsePath = defResponse.absolutePath
        }

        playAudioFile(responsePath)
    }

    private fun playAudioFile(path: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                val vol = preferencesManager.settings.value.responseVolume.coerceIn(0.2f, 1.0f)
                setVolume(vol, vol)
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    lastTriggerFinishedTimestamp = System.currentTimeMillis()
                    checkContinuousLoopOrResume()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    mediaPlayer = null
                    lastTriggerFinishedTimestamp = System.currentTimeMillis()
                    resumeListening()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer?.release()
            mediaPlayer = null
            lastTriggerFinishedTimestamp = System.currentTimeMillis()
            resumeListening()
        }
    }

    private fun checkContinuousLoopOrResume() {
        val settings = preferencesManager.settings.value
        if (settings.continuousSoothingLoop && _isServiceRunning.value) {
            // Briefly check if baby is still making noise / crying
            serviceScope.launch {
                delay(1200)
                resumeListening()
            }
        } else {
            resumeListening()
        }
    }

    fun resumeListening() {
        if (!_isServiceRunning.value) return

        updateNotification(
            title = getString(R.string.service_listening_title),
            text = getString(R.string.service_listening_text)
        )
        audioProcessor.resetDetection()
        startListening()
    }

    fun startCalibrationFlow() {
        if (calibrationJob?.isActive == true) return
        _isCalibrating.value = true
        _monitorStatus.value = MonitorStatus.CALIBRATING
        stopAudioRecord()
        listeningJob?.cancel()

        calibrationJob = serviceScope.launch {
            val sampleRate = audioProcessor.sampleRate
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = max(minBufSize, 4096)

            try {
                val record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                record.startRecording()
                val buf = ShortArray(2048)
                var totalDb = 0.0
                var count = 0

                for (sec in 5 downTo 1) {
                    _calibrationCountdown.value = sec
                    val startSec = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startSec < 1000) {
                        val r = record.read(buf, 0, buf.size)
                        if (r > 0) {
                            var sumSquares = 0.0
                            for (i in 0 until r) {
                                val s = buf[i].toDouble()
                                sumSquares += s * s
                            }
                            val rms = sqrt(sumSquares / r)
                            val db = (20.0 * log10(max(1.0, rms) / 32767.0) + 95.0).toFloat().coerceIn(15f, 100f)
                            totalDb += db
                            count++
                            _currentDecibels.value = db
                        }
                    }
                }
                record.stop()
                record.release()

                val calibratedFloor = if (count > 0) (totalDb / count).toFloat() else 36f
                audioProcessor.updateAmbientFloor(calibratedFloor)
                val currentSettings = preferencesManager.settings.value
                preferencesManager.updateSettings(
                    currentSettings.copy(
                        calibratedAmbientDb = calibratedFloor,
                        thresholdDb = max(42f, calibratedFloor + 8f)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCalibrating.value = false
                _calibrationCountdown.value = 0
                resumeListening()
            }
        }
    }

    private fun stopAudioRecord() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
        }
    }

    private fun stopMonitoring() {
        _isServiceRunning.value = false
        _monitorStatus.value = MonitorStatus.IDLE
        listeningJob?.cancel()
        listeningJob = null
        calibrationJob?.cancel()
        calibrationJob = null
        _isCalibrating.value = false
        stopAudioRecord()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            mediaPlayer = null
        }
        _currentDecibels.value = 0f
        _soundClassification.value = SoundClassification.SILENCE.label
        _matchConfidence.value = 0f
        releaseWakeLock()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.service_notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildServiceNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, HadiCareService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.service_action_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        if (!_isServiceRunning.value) return
        val notification = buildServiceNotification(title, text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopMonitoring()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "hadis_care_monitor_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.hadiscare.app.ACTION_START"
        const val ACTION_STOP = "com.hadiscare.app.ACTION_STOP"
        const val ACTION_PAUSE_LISTENING = "com.hadiscare.app.ACTION_PAUSE_LISTENING"
        const val ACTION_RESUME_LISTENING = "com.hadiscare.app.ACTION_RESUME_LISTENING"
        const val ACTION_SIMULATE_TRIGGER = "com.hadiscare.app.ACTION_SIMULATE_TRIGGER"
        const val ACTION_START_CALIBRATION = "com.hadiscare.app.ACTION_START_CALIBRATION"
        const val ACTION_RELOAD_PROFILE = "com.hadiscare.app.ACTION_RELOAD_PROFILE"

        private val _monitorStatus = MutableStateFlow(MonitorStatus.IDLE)
        val monitorStatus: StateFlow<MonitorStatus> = _monitorStatus.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _currentDecibels = MutableStateFlow(0f)
        val currentDecibels: StateFlow<Float> = _currentDecibels.asStateFlow()

        private val _dominantFrequency = MutableStateFlow(0f)
        val dominantFrequency: StateFlow<Float> = _dominantFrequency.asStateFlow()

        private val _soundClassification = MutableStateFlow("Room Quiet")
        val soundClassification: StateFlow<String> = _soundClassification.asStateFlow()

        private val _matchConfidence = MutableStateFlow(0f)
        val matchConfidence: StateFlow<Float> = _matchConfidence.asStateFlow()

        private val _sample1Similarity = MutableStateFlow(0f)
        val sample1Similarity: StateFlow<Float> = _sample1Similarity.asStateFlow()

        private val _triggerCount = MutableStateFlow(0)
        val triggerCount: StateFlow<Int> = _triggerCount.asStateFlow()

        private val _isCalibrating = MutableStateFlow(false)
        val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

        private val _calibrationCountdown = MutableStateFlow(0)
        val calibrationCountdown: StateFlow<Int> = _calibrationCountdown.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, HadiCareService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, HadiCareService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun simulateTrigger(context: Context) {
            val intent = Intent(context, HadiCareService::class.java).apply {
                action = ACTION_SIMULATE_TRIGGER
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startCalibration(context: Context) {
            val intent = Intent(context, HadiCareService::class.java).apply {
                action = ACTION_START_CALIBRATION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun reloadProfile(context: Context) {
            if (_isServiceRunning.value) {
                val intent = Intent(context, HadiCareService::class.java).apply {
                    action = ACTION_RELOAD_PROFILE
                }
                context.startService(intent)
            }
        }

        fun pauseListening(context: Context) {
            if (_isServiceRunning.value) {
                val intent = Intent(context, HadiCareService::class.java).apply {
                    action = ACTION_PAUSE_LISTENING
                }
                context.startService(intent)
            }
        }

        fun resumeListening(context: Context) {
            if (_isServiceRunning.value) {
                val intent = Intent(context, HadiCareService::class.java).apply {
                    action = ACTION_RESUME_LISTENING
                }
                context.startService(intent)
            }
        }
    }
}

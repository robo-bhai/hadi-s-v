package com.hadiscare.app.audio

import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class SoundClassification(val label: String) {
    SILENCE("Room Quiet"),
    AMBIENT_NOISE("Ambient Noise (Ignored)"),
    TRANSIENT_CLATTER("Clap / Slam (Blocked)"),
    ADULT_VOICE("Adult Voice (Blocked)"),
    BABY_WHISPER("Baby Whimper / Whisper"),
    BABY_CRY("Baby Cry / Weeping"),
    SAMPLE1_MATCH("Sample 1 Voice Matched")
}

data class SpectralBands(
    val lowRatio: Float,            // 0 - 260 Hz (Adult speech fundamental, HVAC, thuds)
    val infantPitchRatio: Float,    // 300 - 750 Hz (Infant cry fundamental)
    val whisperFormantRatio: Float, // 800 - 3200 Hz (Weeping, crying formants, whispers)
    val highNoiseRatio: Float       // > 3200 Hz (Hisses, friction, clatter)
)

data class AudioFeatures(
    val rms: Double,
    val decibels: Float,
    val dominantFrequencyHz: Float,
    val zeroCrossingRate: Int,
    val mfccVector: FloatArray,
    val spectralBands: SpectralBands,
    val classification: SoundClassification,
    val isAdultVoice: Boolean,
    val isTransient: Boolean,
    val sample1Similarity: Float,
    val matchConfidence: Float,
    val isValidTrigger: Boolean
)

data class SampleProfile(
    val averageDb: Float,
    val dominantPitchHz: Float,
    val spectralBands: SpectralBands,
    val mfccFingerprint: FloatArray,
    val averageZcr: Int
)

class AudioProcessor(
    val sampleRate: Int = 22050
) {
    private val classificationHistory = ArrayDeque<SoundClassification>(8)
    private val confidenceHistory = ArrayDeque<Float>(8)
    private var sample1Profile: SampleProfile? = null
    private val sampleProfiles = mutableListOf<SampleProfile>()

    // Previous frame RMS for transient impulse detection
    private var previousRms: Double = 0.0

    // Room ambient noise baseline
    private var ambientNoiseFloor: Float = 36f

    fun setSampleProfiles(profiles: List<SampleProfile>) {
        sampleProfiles.clear()
        sampleProfiles.addAll(profiles)
        if (profiles.isNotEmpty()) {
            sample1Profile = profiles.first()
        }
    }

    fun addSampleProfile(profile: SampleProfile) {
        sampleProfiles.add(profile)
        if (sample1Profile == null) {
            sample1Profile = profile
        }
    }

    fun clearSampleProfiles() {
        sampleProfiles.clear()
        sample1Profile = null
    }

    fun setSample1Profile(profile: SampleProfile) {
        sample1Profile = profile
        if (sampleProfiles.isEmpty()) {
            sampleProfiles.add(profile)
        } else {
            sampleProfiles[0] = profile
        }
    }

    fun getSample1Profile(): SampleProfile? = sample1Profile

    fun getSampleProfiles(): List<SampleProfile> = sampleProfiles

    fun updateAmbientFloor(calibratedDb: Float) {
        ambientNoiseFloor = calibratedDb.coerceIn(25f, 65f)
    }

    fun getAmbientFloor(): Float = ambientNoiseFloor

    /**
     * Advanced Audio Analysis Pipeline:
     * - MFCC voice fingerprint comparison against Sample 1
     * - Adult voice fundamental rejection
     * - Transient clap / door slam rejection
     * - Baby cry formant & weeping resonance detection
     * - Baby whispering & whimpering detection
     */
    fun analyzePcmBuffer(
        buffer: ShortArray,
        readSamples: Int,
        thresholdDb: Float,
        frequencyFilterEnabled: Boolean = true,
        strictSample1Matching: Boolean = false,
        adultSpeechGuard: Boolean = true,
        babyWhisperDetector: Boolean = true,
        transientNoiseRejector: Boolean = true
    ): AudioFeatures {
        if (readSamples <= 0) {
            return AudioFeatures(
                rms = 0.0,
                decibels = 0f,
                dominantFrequencyHz = 0f,
                zeroCrossingRate = 0,
                mfccVector = FloatArray(12),
                spectralBands = SpectralBands(0f, 0f, 0f, 0f),
                classification = SoundClassification.SILENCE,
                isAdultVoice = false,
                isTransient = false,
                sample1Similarity = 0f,
                matchConfidence = 0f,
                isValidTrigger = false
            )
        }

        var sumSquares = 0.0
        var zeroCrossings = 0

        for (i in 0 until readSamples) {
            val s = buffer[i].toDouble()
            sumSquares += s * s

            if (i > 0) {
                if ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0)) {
                    zeroCrossings++
                }
            }
        }

        val rms = sqrt(sumSquares / readSamples)
        val normalizedRatio = max(1.0, rms) / 32767.0
        val rawDb = (20.0 * log10(normalizedRatio) + 95.0).toFloat().coerceIn(15f, 100f)

        // Smooth ambient floor tracker
        ambientNoiseFloor = (0.98f * ambientNoiseFloor) + (0.02f * rawDb)

        val zcrNormalized = ((zeroCrossings.toFloat() / readSamples) * 1024f).toInt()
        val pitch = estimateDominantFrequency(buffer, readSamples, sampleRate)
        val spectralBands = calculateSpectralBands(buffer, readSamples, sampleRate)
        val mfccs = computeMfcc(buffer, readSamples, sampleRate)

        // 1. Transient Clatter / Clap / Door Slam Filter
        // Claps/slams produce sudden massive delta rise followed by silence
        val rmsRatio = if (previousRms > 50.0) rms / previousRms else 1.0
        val isTransient = (rmsRatio > 6.0 && rawDb > 62f) || (zcrNormalized > 230 && rawDb > 65f)
        previousRms = rms

        // 2. Adult Voice Detection
        // Adult speech fundamental is 80 - 285 Hz with high energy in 0 - 260 Hz
        val isAdultPitch = pitch in 80f..285f
        val hasDominantAdultLowEnergy = spectralBands.lowRatio > 0.32f && spectralBands.infantPitchRatio < 0.30f
        val isAdultVoice = isAdultPitch || hasDominantAdultLowEnergy

        // 3. Multi-Sample MFCC & Pitch Fingerprint Similarity
        val allProfiles = if (sampleProfiles.isNotEmpty()) sampleProfiles else listOfNotNull(sample1Profile)
        var s1Similarity = 0f
        for (profile in allProfiles) {
            val sim = calculateProfileSimilarity(pitch, spectralBands, mfccs, profile)
            if (sim > s1Similarity) {
                s1Similarity = sim
            }
        }

        // 4. Baby Cry & Weeping Detection (General baby models)
        val isBabyCryPitch = pitch in 320f..780f
        val hasInfantEnergy = (spectralBands.infantPitchRatio + spectralBands.whisperFormantRatio) >= 0.54f
        val isBabyCry = isBabyCryPitch && hasInfantEnergy && !isAdultVoice

        // 5. Baby Whispering & Soft Whimper Detection
        val isWhisperEnergy = spectralBands.whisperFormantRatio >= 0.40f && spectralBands.lowRatio < 0.22f
        val isBabyWhisper = isWhisperEnergy && zcrNormalized in 65..195 && !isAdultVoice

        // Effective threshold considering room baseline
        val effectiveThreshold = max(thresholdDb, ambientNoiseFloor + 6f)
        val isAudible = rawDb >= effectiveThreshold

        // Classify Sound:
        // When strictSample1Matching is ON, only match if s1Similarity meets the fingerprint threshold against any registered sample
        val isSample1Matched = allProfiles.isNotEmpty() && s1Similarity >= if (strictSample1Matching) 0.60f else 0.52f

        val classification: SoundClassification = when {
            rawDb < 34f -> SoundClassification.SILENCE
            transientNoiseRejector && isTransient -> SoundClassification.TRANSIENT_CLATTER
            adultSpeechGuard && isAdultVoice -> SoundClassification.ADULT_VOICE
            isSample1Matched -> SoundClassification.SAMPLE1_MATCH
            strictSample1Matching -> SoundClassification.AMBIENT_NOISE // In strict mode, general sounds without fingerprint match are ignored
            isBabyCry -> SoundClassification.BABY_CRY
            babyWhisperDetector && isBabyWhisper -> SoundClassification.BABY_WHISPER
            else -> SoundClassification.AMBIENT_NOISE
        }

        // Confidence calculation
        val confidence = when (classification) {
            SoundClassification.SAMPLE1_MATCH -> max(s1Similarity, 0.72f)
            SoundClassification.BABY_CRY -> (0.68f + (spectralBands.infantPitchRatio * 0.30f)).coerceIn(0.6f, 0.98f)
            SoundClassification.BABY_WHISPER -> (0.62f + (spectralBands.whisperFormantRatio * 0.32f)).coerceIn(0.55f, 0.95f)
            SoundClassification.TRANSIENT_CLATTER -> 0.05f
            SoundClassification.ADULT_VOICE -> 0.08f
            SoundClassification.AMBIENT_NOISE -> 0.12f
            SoundClassification.SILENCE -> 0.0f
        }

        // Trigger Evaluation:
        // Claps, door slams, adult speech, and ambient noise are strictly rejected.
        // In strictSample1Matching mode, ONLY confirmed SAMPLE1_MATCH with valid fingerprint can trigger.
        val isValidTrigger = if (!isAudible || isTransient) {
            false
        } else if (adultSpeechGuard && isAdultVoice) {
            false
        } else if (strictSample1Matching) {
            classification == SoundClassification.SAMPLE1_MATCH && s1Similarity >= 0.60f && confidence >= 0.60f
        } else if (classification == SoundClassification.SAMPLE1_MATCH) {
            s1Similarity >= 0.52f && confidence >= 0.55f
        } else {
            (classification == SoundClassification.BABY_CRY && confidence >= 0.60f) ||
            (babyWhisperDetector && classification == SoundClassification.BABY_WHISPER && confidence >= 0.58f)
        }

        return AudioFeatures(
            rms = rms,
            decibels = rawDb,
            dominantFrequencyHz = pitch,
            zeroCrossingRate = zcrNormalized,
            mfccVector = mfccs,
            spectralBands = spectralBands,
            classification = classification,
            isAdultVoice = isAdultVoice,
            isTransient = isTransient,
            sample1Similarity = s1Similarity,
            matchConfidence = confidence,
            isValidTrigger = isValidTrigger
        )
    }

    /**
     * Temporal multi-frame stability check.
     */
    fun evaluateTriggerMatch(
        features: AudioFeatures,
        requiredDurationMs: Int,
        chunkDurationMs: Long,
        strictSample1Only: Boolean = false
    ): Boolean {
        if (classificationHistory.size >= 6) {
            classificationHistory.removeFirst()
            confidenceHistory.removeFirst()
        }

        classificationHistory.addLast(features.classification)
        confidenceHistory.addLast(features.matchConfidence)

        val validMatches = classificationHistory.count {
            if (strictSample1Only) {
                it == SoundClassification.SAMPLE1_MATCH
            } else {
                it == SoundClassification.SAMPLE1_MATCH ||
                it == SoundClassification.BABY_CRY ||
                it == SoundClassification.BABY_WHISPER
            }
        }

        val noAdultOrTransient = classificationHistory.none {
            it == SoundClassification.ADULT_VOICE || it == SoundClassification.TRANSIENT_CLATTER
        }

        val shouldTrigger = features.isValidTrigger &&
                validMatches >= 2 &&
                noAdultOrTransient &&
                features.matchConfidence >= 0.60f

        if (shouldTrigger) {
            resetDetection()
            return true
        }

        return false
    }

    fun resetDetection() {
        classificationHistory.clear()
        confidenceHistory.clear()
    }

    /**
     * 12-coefficient MFCC (Mel-Frequency Cepstral Coefficients) extractor.
     */
    private fun computeMfcc(buffer: ShortArray, size: Int, rate: Int): FloatArray {
        val numFilters = 16
        val numCepstra = 12
        val melEnergies = DoubleArray(numFilters)

        // Mel filter center frequencies (100 Hz to 5500 Hz)
        val melMin = hzToMel(100.0)
        val melMax = hzToMel(5500.0)
        val melStep = (melMax - melMin) / (numFilters + 1)

        val centerFreqs = DoubleArray(numFilters) { i ->
            melToHz(melMin + (i + 1) * melStep)
        }

        for (f in 0 until numFilters) {
            val centerHz = centerFreqs[f]
            val k = (0.5 + (size * centerHz / rate)).toInt()
            val omega = (2.0 * Math.PI * k) / size
            val coeff = 2.0 * cos(omega)

            var q1 = 0.0
            var q2 = 0.0
            val step = max(1, size / 256)
            for (i in 0 until size step step) {
                val q0 = coeff * q1 - q2 + buffer[i].toDouble()
                q2 = q1
                q1 = q0
            }
            val power = q1 * q1 + q2 * q2 - coeff * q1 * q2
            melEnergies[f] = log10(max(1.0, power))
        }

        // Discrete Cosine Transform (DCT-II)
        val mfccs = FloatArray(numCepstra)
        for (i in 0 until numCepstra) {
            var sum = 0.0
            for (j in 0 until numFilters) {
                sum += melEnergies[j] * cos(PI * i * (j + 0.5) / numFilters)
            }
            mfccs[i] = sum.toFloat()
        }

        // Normalize
        var normSum = 0.0
        for (v in mfccs) normSum += v * v
        val norm = sqrt(normSum)
        if (norm > 0.0) {
            for (i in mfccs.indices) mfccs[i] = (mfccs[i] / norm).toFloat()
        }

        return mfccs
    }

    private fun hzToMel(hz: Double): Double = 2595.0 * log10(1.0 + hz / 700.0)
    private fun melToHz(mel: Double): Double = 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)

    fun calculateProfileSimilarity(
        pitch: Float,
        spectralBands: SpectralBands,
        mfccs: FloatArray,
        profile: SampleProfile
    ): Float {
        val pitchDelta = abs(pitch - profile.dominantPitchHz)
        val pitchProximity = when {
            pitchDelta < 45f -> 1.0f
            pitchDelta < 90f -> 0.85f
            pitchDelta < 140f -> 0.65f
            pitchDelta < 200f -> 0.35f
            pitchDelta < 260f -> 0.10f
            else -> 0.0f
        }

        if (pitchProximity <= 0.02f) {
            return 0f
        }

        val mfccCosSim = cosineSimilarity(mfccs, profile.mfccFingerprint)
        val bandScore = (
            (1f - abs(spectralBands.infantPitchRatio - profile.spectralBands.infantPitchRatio) * 1.5f).coerceIn(0f, 1f) * 0.45f +
            (1f - abs(spectralBands.whisperFormantRatio - profile.spectralBands.whisperFormantRatio) * 1.5f).coerceIn(0f, 1f) * 0.35f +
            (1f - abs(spectralBands.lowRatio - profile.spectralBands.lowRatio) * 2.0f).coerceIn(0f, 1f) * 0.20f
        ).coerceIn(0f, 1f)

        val timbreScore = (mfccCosSim * 0.65f + bandScore * 0.35f).coerceIn(0f, 1f)
        return (timbreScore * (0.35f + 0.65f * pitchProximity)).coerceIn(0f, 1f)
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return 0f
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        if (denom <= 0.0) return 0f
        val cos = (dot / denom).toFloat()
        return ((cos + 1f) / 2f).coerceIn(0f, 1f)
    }

    private fun calculateSpectralBands(buffer: ShortArray, size: Int, rate: Int): SpectralBands {
        var lowEnergy = 0.0
        var infantEnergy = 0.0
        var whisperEnergy = 0.0
        var highEnergy = 0.0

        val testFreqs = intArrayOf(
            120, 180, 230,
            380, 450, 560, 680,
            950, 1400, 2100, 2800,
            3800, 5200
        )

        for (targetFreq in testFreqs) {
            val k = (0.5 + (size * targetFreq.toDouble() / rate)).toInt()
            val omega = (2.0 * Math.PI * k) / size
            val coeff = 2.0 * cos(omega)

            var q1 = 0.0
            var q2 = 0.0
            val step = max(1, size / 512)
            for (i in 0 until size step step) {
                val q0 = coeff * q1 - q2 + buffer[i].toDouble()
                q2 = q1
                q1 = q0
            }

            val power = q1 * q1 + q2 * q2 - coeff * q1 * q2
            val normPower = max(0.0, power)

            when (targetFreq) {
                in 0..260 -> lowEnergy += normPower
                in 300..750 -> infantEnergy += normPower
                in 800..3200 -> whisperEnergy += normPower
                else -> highEnergy += normPower
            }
        }

        val total = lowEnergy + infantEnergy + whisperEnergy + highEnergy
        return if (total > 0.0) {
            SpectralBands(
                lowRatio = (lowEnergy / total).toFloat(),
                infantPitchRatio = (infantEnergy / total).toFloat(),
                whisperFormantRatio = (whisperEnergy / total).toFloat(),
                highNoiseRatio = (highEnergy / total).toFloat()
            )
        } else {
            SpectralBands(0.25f, 0.25f, 0.25f, 0.25f)
        }
    }

    private fun estimateDominantFrequency(buffer: ShortArray, size: Int, rate: Int): Float {
        val minFreq = 85
        val maxFreq = 950
        val minLag = rate / maxFreq
        val maxLag = (rate / minFreq).coerceAtMost(size / 2)

        if (minLag >= maxLag || maxLag >= size) return 0f

        val limit = (size - maxLag).coerceAtMost(1024)
        if (limit <= 0) return 0f

        val correlations = DoubleArray(maxLag + 1)
        var maxCorrelation = 0.0

        for (lag in minLag..maxLag) {
            var sum = 0.0
            for (i in 0 until limit step 2) {
                sum += buffer[i].toDouble() * buffer[i + lag].toDouble()
            }
            correlations[lag] = sum
            if (sum > maxCorrelation) {
                maxCorrelation = sum
            }
        }

        if (maxCorrelation <= 0.0) return 0f

        // Peak picking: find the first prominent local maximum (fundamental frequency)
        // to avoid harmonic sub-octave multiples (e.g. 500Hz picked at 250Hz lag).
        val threshold = maxCorrelation * 0.80
        var bestLag = 0
        for (lag in (minLag + 1) until maxLag) {
            val curr = correlations[lag]
            if (curr >= threshold && curr >= correlations[lag - 1] && curr >= correlations[lag + 1]) {
                bestLag = lag
                break
            }
        }

        if (bestLag == 0) {
            var highestVal = 0.0
            for (lag in minLag..maxLag) {
                if (correlations[lag] > highestVal) {
                    highestVal = correlations[lag]
                    bestLag = lag
                }
            }
        }

        return if (bestLag > 0) {
            (rate.toFloat() / bestLag).coerceIn(0f, 1500f)
        } else {
            0f
        }
    }

    fun analyzeSampleFile(file: File): SampleProfile {
        if (!file.exists() || file.length() < 100) {
            val defaultBands = SpectralBands(0.10f, 0.58f, 0.28f, 0.04f)
            val defaultMfcc = FloatArray(12) { 0.1f }
            return SampleProfile(55f, 450f, defaultBands, defaultMfcc, 75).also { sample1Profile = it }
        }

        try {
            // Decode file to raw 16-bit PCM ShortArray using AudioFileDecoder (supports MP4, M4A, AAC, MP3, WAV)
            val pcmData = AudioFileDecoder.decodeToShortArray(file, maxDurationSec = 8, targetSampleRate = sampleRate)
            if (pcmData != null && pcmData.size >= 512) {
                val chunkSize = 2048
                var offset = 0
                var totalDbSum = 0.0
                var totalPitchSum = 0.0
                var pitchCount = 0
                var totalZcr = 0
                var chunkCount = 0

                var sumLow = 0f
                var sumInfant = 0f
                var sumWhisper = 0f
                var sumHigh = 0f
                val avgMfccs = FloatArray(12)

                val chunk = ShortArray(chunkSize)
                while (offset + chunkSize <= pcmData.size && chunkCount < 40) {
                    System.arraycopy(pcmData, offset, chunk, 0, chunkSize)
                    offset += chunkSize

                    val features = analyzePcmBuffer(
                        chunk, chunkSize, 40f,
                        frequencyFilterEnabled = false,
                        strictSample1Matching = false,
                        adultSpeechGuard = false,
                        babyWhisperDetector = true,
                        transientNoiseRejector = false
                    )

                    totalDbSum += features.decibels
                    if (features.dominantFrequencyHz in 150f..950f) {
                        totalPitchSum += features.dominantFrequencyHz
                        pitchCount++
                    }
                    totalZcr += features.zeroCrossingRate
                    sumLow += features.spectralBands.lowRatio
                    sumInfant += features.spectralBands.infantPitchRatio
                    sumWhisper += features.spectralBands.whisperFormantRatio
                    sumHigh += features.spectralBands.highNoiseRatio

                    for (m in 0 until 12) {
                        avgMfccs[m] += features.mfccVector[m]
                    }

                    chunkCount++
                }

                if (chunkCount > 0) {
                    val avgDb = (totalDbSum / chunkCount).toFloat()
                    val avgPitch = if (pitchCount > 0) (totalPitchSum / pitchCount).toFloat() else 450f
                    val avgZcr = totalZcr / chunkCount
                    for (m in 0 until 12) avgMfccs[m] /= chunkCount
                    val avgBands = SpectralBands(
                        sumLow / chunkCount,
                        sumInfant / chunkCount,
                        sumWhisper / chunkCount,
                        sumHigh / chunkCount
                    )

                    val profile = SampleProfile(
                        averageDb = avgDb,
                        dominantPitchHz = avgPitch,
                        spectralBands = avgBands,
                        mfccFingerprint = avgMfccs,
                        averageZcr = avgZcr
                    )
                    sample1Profile = profile
                    return profile
                }
            }

            // Fallback direct WAV streaming if codec unavailable
            FileInputStream(file).use { input ->
                val bytes = ByteArray(4096)
                var bytesRead: Int
                var totalDbSum = 0.0
                var totalPitchSum = 0.0
                var pitchCount = 0
                var totalZcr = 0
                var chunkCount = 0

                var sumLow = 0f
                var sumInfant = 0f
                var sumWhisper = 0f
                var sumHigh = 0f
                val avgMfccs = FloatArray(12)

                input.skip(44) // Skip WAV header

                while (input.read(bytes).also { bytesRead = it } != -1) {
                    val shortCount = bytesRead / 2
                    if (shortCount <= 0) break

                    val byteBuffer = ByteBuffer.wrap(bytes, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN)
                    val shorts = ShortArray(shortCount)
                    for (i in 0 until shortCount) {
                        shorts[i] = byteBuffer.short
                    }

                    val features = analyzePcmBuffer(
                        shorts, shortCount, 40f,
                        frequencyFilterEnabled = false,
                        strictSample1Matching = false,
                        adultSpeechGuard = false,
                        babyWhisperDetector = true,
                        transientNoiseRejector = false
                    )

                    totalDbSum += features.decibels
                    if (features.dominantFrequencyHz in 150f..950f) {
                        totalPitchSum += features.dominantFrequencyHz
                        pitchCount++
                    }
                    totalZcr += features.zeroCrossingRate
                    sumLow += features.spectralBands.lowRatio
                    sumInfant += features.spectralBands.infantPitchRatio
                    sumWhisper += features.spectralBands.whisperFormantRatio
                    sumHigh += features.spectralBands.highNoiseRatio

                    for (m in 0 until 12) {
                        avgMfccs[m] += features.mfccVector[m]
                    }

                    chunkCount++
                    if (chunkCount >= 40) break
                }

                val avgDb = if (chunkCount > 0) (totalDbSum / chunkCount).toFloat() else 55f
                val avgPitch = if (pitchCount > 0) (totalPitchSum / pitchCount).toFloat() else 450f
                val avgZcr = if (chunkCount > 0) totalZcr / chunkCount else 75

                if (chunkCount > 0) {
                    for (m in 0 until 12) avgMfccs[m] /= chunkCount
                }

                val avgBands = if (chunkCount > 0) {
                    SpectralBands(
                        sumLow / chunkCount,
                        sumInfant / chunkCount,
                        sumWhisper / chunkCount,
                        sumHigh / chunkCount
                    )
                } else {
                    SpectralBands(0.10f, 0.58f, 0.28f, 0.04f)
                }

                val profile = SampleProfile(
                    averageDb = avgDb,
                    dominantPitchHz = avgPitch,
                    spectralBands = avgBands,
                    mfccFingerprint = avgMfccs,
                    averageZcr = avgZcr
                )
                sample1Profile = profile
                return profile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val defaultBands = SpectralBands(0.10f, 0.58f, 0.28f, 0.04f)
            val defaultMfcc = FloatArray(12) { 0.1f }
            return SampleProfile(55f, 450f, defaultBands, defaultMfcc, 75).also { sample1Profile = it }
        }
    }
}

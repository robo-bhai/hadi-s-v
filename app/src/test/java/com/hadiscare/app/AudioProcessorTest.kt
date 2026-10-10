package com.hadiscare.app

import com.hadiscare.app.audio.AudioProcessor
import com.hadiscare.app.audio.SampleProfile
import com.hadiscare.app.audio.SoundClassification
import com.hadiscare.app.audio.SpectralBands
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class AudioProcessorTest {

    private val audioProcessor = AudioProcessor(sampleRate = 22050)

    @Test
    fun testSilenceProducesLowDecibelsAndNoTrigger() {
        val silence = ShortArray(2048) { 0 }
        val features = audioProcessor.analyzePcmBuffer(
            buffer = silence,
            readSamples = silence.size,
            thresholdDb = 48f,
            frequencyFilterEnabled = true
        )

        assertTrue("Silence should produce low dB", features.decibels < 35f)
        assertFalse("Silence should not trigger", features.isValidTrigger)
    }

    @Test
    fun testAdultVoiceIsStrictlyRejected() {
        val sampleRate = 22050
        val adultPitch = 130.0 // Adult male voice pitch fundamental
        val testBuffer = ShortArray(2048) { i ->
            (sin(2.0 * PI * adultPitch * (i.toDouble() / sampleRate)) * 25000).toInt().toShort()
        }

        val features = audioProcessor.analyzePcmBuffer(
            buffer = testBuffer,
            readSamples = testBuffer.size,
            thresholdDb = 45f,
            frequencyFilterEnabled = true
        )

        assertTrue("130Hz should be recognized as adult voice", features.isAdultVoice)
        assertEquals(SoundClassification.ADULT_VOICE, features.classification)
        assertFalse("Adult voice must NOT trigger lullaby", features.isValidTrigger)
    }

    @Test
    fun testBabyCryAcousticIsDetectedAndTriggers() {
        audioProcessor.resetDetection()
        val sampleRate = 22050
        val babyPitch = 450.0 // Infant cry fundamental frequency
        val testBuffer = ShortArray(2048) { i ->
            (sin(2.0 * PI * babyPitch * (i.toDouble() / sampleRate)) * 25000).toInt().toShort()
        }

        val features = audioProcessor.analyzePcmBuffer(
            buffer = testBuffer,
            readSamples = testBuffer.size,
            thresholdDb = 45f,
            frequencyFilterEnabled = true
        )

        assertFalse("450Hz is not adult voice", features.isAdultVoice)
        assertTrue("450Hz baby pitch should be recognized as baby cry", features.classification == SoundClassification.BABY_CRY)
        assertTrue("Baby cry should be valid trigger", features.isValidTrigger)

        // Test multi-frame confirmation
        audioProcessor.evaluateTriggerMatch(features, requiredDurationMs = 450, chunkDurationMs = 100)
        val triggered = audioProcessor.evaluateTriggerMatch(features, requiredDurationMs = 450, chunkDurationMs = 100)
        assertTrue("Consistent baby cry should trigger", triggered)
    }

    @Test
    fun testStrictSample1Matching() {
        val profile = SampleProfile(
            averageDb = 58f,
            dominantPitchHz = 500f,
            spectralBands = SpectralBands(0.08f, 0.60f, 0.28f, 0.04f),
            mfccFingerprint = FloatArray(12) { 0.2f },
            averageZcr = 75
        )
        audioProcessor.setSample1Profile(profile)

        val sampleRate = 22050
        // Sound matching Sample 1 (500 Hz)
        val matchBuffer = ShortArray(2048) { i ->
            (sin(2.0 * PI * 500.0 * (i.toDouble() / sampleRate)) * 24000).toInt().toShort()
        }

        val features = audioProcessor.analyzePcmBuffer(
            buffer = matchBuffer,
            readSamples = matchBuffer.size,
            thresholdDb = 45f,
            frequencyFilterEnabled = true,
            strictSample1Matching = true
        )

        assertTrue("Matching sound should yield high similarity", features.sample1Similarity >= 0.65f)
        assertEquals(SoundClassification.SAMPLE1_MATCH, features.classification)
        assertTrue("Sample 1 match should trigger in strict mode", features.isValidTrigger)
    }
}

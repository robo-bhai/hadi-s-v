package com.hadiscare.app.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object DefaultAudioGenerator {

    private const val SAMPLE_RATE = 22050

    /**
     * Ensures default sample files exist in the app's internal storage.
     * Returns a pair of (triggerFile, responseFile).
     */
    fun ensureDefaultAudioFiles(context: Context): Pair<File, File> {
        val triggerFile = File(context.filesDir, "default_trigger_cry.wav")
        if (!triggerFile.exists() || triggerFile.length() < 1000) {
            generateInfantCryWav(triggerFile)
        }

        val responseFile = File(context.filesDir, "default_soothing_lullaby.wav")
        if (!responseFile.exists() || responseFile.length() < 1000) {
            generateLullabyWav(responseFile)
        }

        return Pair(triggerFile, responseFile)
    }

    /**
     * Generates a 4-second infant cry acoustic wave pattern.
     * Characteristics: fundamental pitch ~450 Hz with harmonics at 900Hz and 1350Hz,
     * amplitude modulation simulating natural rhythmic baby wails.
     */
    private fun generateInfantCryWav(file: File) {
        val durationSec = 4.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcmData = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 1.2 Hz wailing rhythm (bursts of crying with breath pauses)
            val wailEnvelope = ((sin(2.0 * PI * 1.2 * t) + 1.0) / 2.0).coerceIn(0.0, 1.0)

            // Pitch modulation around 440-480 Hz
            val pitch = 450.0 + 30.0 * sin(2.0 * PI * 3.5 * t)
            val f0 = sin(2.0 * PI * pitch * t)
            val f1 = 0.5 * sin(2.0 * PI * (pitch * 2.0) * t)
            val f2 = 0.25 * sin(2.0 * PI * (pitch * 3.0) * t)

            val combined = (f0 + f1 + f2) / 1.75
            val sampleVal = (combined * wailEnvelope * 24000).toInt().coerceIn(-32767, 32767)
            pcmData[i] = sampleVal.toShort()
        }

        writeWavFile(file, pcmData, SAMPLE_RATE)
    }

    /**
     * Generates a soothing 7-second music box melody (Brahms' Lullaby opening notes).
     * Gentle decay envelope for each note creates a calming bell/music-box chime.
     */
    private fun generateLullabyWav(file: File) {
        // Notes: G4, G4, Bb4, G4, G4, Bb4, G4, Bb4, Eb5, D5, C5, C5, Bb4...
        // Frequencies in Hz:
        // Eb4=311.13, F4=349.23, G4=392.00, Ab4=415.30, Bb4=466.16, C5=523.25, D5=587.33, Eb5=622.25
        val melodyNotes = listOf(
            Pair(392.00, 0.45), // G4
            Pair(392.00, 0.45), // G4
            Pair(466.16, 0.90), // Bb4
            Pair(392.00, 0.45), // G4
            Pair(392.00, 0.45), // G4
            Pair(466.16, 0.90), // Bb4
            Pair(392.00, 0.45), // G4
            Pair(466.16, 0.45), // Bb4
            Pair(622.25, 0.90), // Eb5
            Pair(587.33, 0.90), // D5
            Pair(523.25, 0.60), // C5
            Pair(466.16, 1.20)  // Bb4
        )

        var totalDuration = 0.0
        melodyNotes.forEach { totalDuration += it.second }
        val totalSamples = (SAMPLE_RATE * totalDuration).toInt()
        val pcmData = ShortArray(totalSamples)

        var currentSampleIndex = 0
        for ((frequency, noteDuration) in melodyNotes) {
            val noteSampleCount = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until noteSampleCount) {
                if (currentSampleIndex + i >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE

                // Music box / chime envelope: sudden strike, smooth exponential decay
                val envelope = exp(-3.2 * t)

                // Bell sound: fundamental + gentle harmonic
                val s1 = sin(2.0 * PI * frequency * t)
                val s2 = 0.3 * sin(2.0 * PI * (frequency * 2.0) * t)
                val s3 = 0.15 * sin(2.0 * PI * (frequency * 3.0) * t)

                val sample = ((s1 + s2 + s3) * envelope * 20000).toInt().coerceIn(-32767, 32767)
                pcmData[currentSampleIndex + i] = sample.toShort()
            }
            currentSampleIndex += noteSampleCount
        }

        writeWavFile(file, pcmData, SAMPLE_RATE)
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = (pcmData.size * 2).toLong()
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = (sampleRate * channels * 2).toLong()

        FileOutputStream(file).use { out ->
            writeWavHeader(out, totalAudioLen, totalDataLen, sampleRate.toLong(), channels, byteRate)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                byteBuffer.putShort(s)
            }
            out.write(byteBuffer.array())
            out.flush()
        }
    }

    private fun writeWavHeader(
        out: OutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        sampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }
}

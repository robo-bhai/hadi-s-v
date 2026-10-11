package com.hadiscare.app.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Universal Audio Decoder: extracts 16-bit PCM (ShortArray) from WAV, M4A/AAC, MP3, etc.
 * Uses MediaCodec & MediaExtractor on Android, with a clean WAV direct-stream fallback.
 */
object AudioFileDecoder {

    fun decodeToShortArray(file: File, maxDurationSec: Int = 10, targetSampleRate: Int = 22050): ShortArray? {
        if (!file.exists() || file.length() < 100) return null

        // Try fast WAV parsing first if file is WAV format
        val wavPcm = tryParseWavFile(file, maxDurationSec)
        if (wavPcm != null && wavPcm.isNotEmpty()) {
            return wavPcm
        }

        // Use Android MediaExtractor & MediaCodec for MP4, M4A, AAC, MP3
        return try {
            decodeWithMediaCodec(file, maxDurationSec, targetSampleRate)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun tryParseWavFile(file: File, maxDurationSec: Int): ShortArray? {
        return try {
            FileInputStream(file).use { input ->
                val header = ByteArray(44)
                val readHeader = input.read(header)
                if (readHeader < 44) return null

                // Verify "RIFF" and "WAVE"
                val isRiff = header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() &&
                        header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte()
                val isWave = header[8] == 'W'.code.toByte() && header[9] == 'A'.code.toByte() &&
                        header[10] == 'V'.code.toByte() && header[11] == 'E'.code.toByte()
                if (!isRiff || !isWave) return null

                val maxBytes = maxDurationSec * 22050 * 2 * 2
                val pcmBytes = ByteArray(maxBytes.coerceAtMost(file.length().toInt()))
                val bytesRead = input.read(pcmBytes)
                if (bytesRead <= 0) return null

                val shortCount = bytesRead / 2
                val buffer = ByteBuffer.wrap(pcmBytes, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN)
                val shorts = ShortArray(shortCount)
                for (i in 0 until shortCount) {
                    shorts[i] = buffer.short
                }
                shorts
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeWithMediaCodec(file: File, maxDurationSec: Int, targetSampleRate: Int): ShortArray? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        return try {
            extractor.setDataSource(file.absolutePath)
            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex < 0 || format == null) {
                extractor.release()
                return null
            }

            extractor.selectTrack(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val maxSamples = targetSampleRate * maxDurationSec
            val pcmList = ArrayList<Short>(maxSamples)
            val bufferInfo = MediaCodec.BufferInfo()
            var isEos = false

            val timeoutUs = 5000L

            while (pcmList.size < maxSamples) {
                if (!isEos) {
                    val inIndex = codec.dequeueInputBuffer(timeoutUs)
                    if (inIndex >= 0) {
                        val inBuffer = codec.getInputBuffer(inIndex)
                        if (inBuffer != null) {
                            val sampleSize = extractor.readSampleData(inBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isEos = true
                            } else {
                                codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outIndex >= 0) {
                    val outBuffer = codec.getOutputBuffer(outIndex)
                    if (outBuffer != null && bufferInfo.size > 0) {
                        outBuffer.position(bufferInfo.offset)
                        outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        outBuffer.order(ByteOrder.LITTLE_ENDIAN)

                        val shortBuf = outBuffer.asShortBuffer()
                        while (shortBuf.hasRemaining() && pcmList.size < maxSamples) {
                            pcmList.add(shortBuf.get())
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER && isEos) {
                    break
                }
            }

            val result = ShortArray(pcmList.size)
            for (i in pcmList.indices) {
                result[i] = pcmList[i]
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (e: Exception) {}
            try {
                extractor.release()
            } catch (e: Exception) {}
        }
    }
}

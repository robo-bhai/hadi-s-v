package com.hadiscare.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording: Boolean = false

    fun startRecording(targetFileName: String): File? {
        stopRecording()
        val file = File(context.filesDir, targetFileName)
        outputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        isRecording = true
        return file
    }

    fun stopRecording(): File? {
        if (!isRecording) return outputFile
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            recorder = null
            isRecording = false
            null
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}

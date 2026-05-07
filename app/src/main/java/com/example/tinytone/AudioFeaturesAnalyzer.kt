package com.example.tinytone

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

object AudioFeaturesAnalyzer {

    private const val SAMPLE_RATE = 44100
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    data class AudioFeatures(
        val rms: Double,
        val zcr: Int
    )

    @SuppressLint("MissingPermission")
    suspend fun recordAndAnalyze(
        durationMs: Long,
        onProgress: (AudioFeatures) -> Unit
    ): AudioFeatures = withContext(Dispatchers.IO) {
        
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        val buffer = ShortArray(bufferSize)
        val startTime = System.currentTimeMillis()
        
        var totalRms = 0.0
        var totalZcr = 0
        var readCount = 0

        while (isRecording && (System.currentTimeMillis() - startTime) < durationMs) {
            val numRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (numRead > 0) {
                var sumSquares = 0.0
                var zeroCrossings = 0
                
                for (i in 0 until numRead) {
                    val sample = buffer[i].toDouble()
                    sumSquares += sample * sample
                    
                    if (i > 0) {
                        val prevSample = buffer[i - 1].toDouble()
                        if ((sample >= 0 && prevSample < 0) || (sample < 0 && prevSample >= 0)) {
                            zeroCrossings++
                        }
                    }
                }
                
                val currentRms = sqrt(sumSquares / numRead)
                // Normalize ZCR so it approximates a frequency over a second scale or just per buffer
                // The buffer represents (numRead / SAMPLE_RATE) seconds.
                val currentZcr = zeroCrossings
                
                totalRms += currentRms
                totalZcr += currentZcr
                readCount++
                
                withContext(Dispatchers.Main) {
                    onProgress(AudioFeatures(currentRms, currentZcr))
                }
            }
        }

        stopRecording()

        val avgRms = if (readCount > 0) totalRms / readCount else 0.0
        // Total ZCR over the entire duration gives us an estimate of pitch
        AudioFeatures(avgRms, totalZcr)
    }

    fun stopRecording() {
        if (isRecording) {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }
}

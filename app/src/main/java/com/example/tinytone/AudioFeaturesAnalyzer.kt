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
        val pitchHz: Int
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
        var nonSilentReadCount = 0
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
                
                val currentRms = kotlin.math.sqrt(sumSquares / numRead)
                
                totalRms += currentRms
                readCount++
                if (currentRms > 150.0) {
                    totalZcr += zeroCrossings
                    nonSilentReadCount++
                }
                
                val currentPitchHz = if (currentRms > 150.0) {
                    val durationSec = numRead.toDouble() / SAMPLE_RATE
                    ((zeroCrossings / durationSec) / 2.0).toInt()
                } else 0
                
                withContext(Dispatchers.Main) {
                    onProgress(AudioFeatures(currentRms, currentPitchHz))
                }
            }
        }

        stopRecording()

        val avgRms = if (readCount > 0) totalRms / readCount else 0.0
        val nonSilentSeconds = (nonSilentReadCount * buffer.size).toDouble() / SAMPLE_RATE
        val estimatedPitchHz = if (nonSilentSeconds > 0) {
            (totalZcr / nonSilentSeconds) / 2.0
        } else 0.0
        
        AudioFeatures(avgRms, estimatedPitchHz.toInt())
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

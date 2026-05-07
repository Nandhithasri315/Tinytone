package com.example.tinytone

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoudnessPracticeActivity : AppCompatActivity() {

    private lateinit var btnRecord: MaterialButton
    private lateinit var tvTargetVolume: TextView
    private lateinit var tvStatus: TextView
    private lateinit var waveformView: WaveformView

    private var isRecording = false
    private var isTargetLoud = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loudness_practice)

        btnRecord = findViewById(R.id.btnRecord)
        tvTargetVolume = findViewById(R.id.tvTargetVolume)
        tvStatus = findViewById(R.id.tvStatus)
        waveformView = findViewById(R.id.waveformView)

        findViewById<AppCompatImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        setNewTarget()

        btnRecord.setOnClickListener {
            if (!isRecording) {
                if (checkPermission()) {
                    startRecording()
                }
            } else {
                stopRecording()
            }
        }
    }

    private fun setNewTarget() {
        isTargetLoud = Random.nextBoolean()
        tvTargetVolume.text = if (isTargetLoud) "LOUD!" else "SOFT"
        tvTargetVolume.setTextColor(ContextCompat.getColor(this, if (isTargetLoud) R.color.card_loudness else R.color.sky_blue))
        tvStatus.text = "Tap to practice your volume control"
        updateRecordButtonIdle()
    }

    private fun startRecording() {
        isRecording = true
        waveformView.clear()
        tvStatus.text = "Recording 3 seconds..."
        updateRecordButtonListening()

        lifecycleScope.launch {
            val result = AudioFeaturesAnalyzer.recordAndAnalyze(3000L) { features ->
                // Draw waveform scale. Using RMS / 10 to scale down roughly.
                val amp = (features.rms / 10f).toFloat().coerceAtMost(200f)
                waveformView.addAmplitude(amp)
            }
            
            isRecording = false
            updateRecordButtonIdle()
            evaluateResult(result.rms)
        }
    }

    private fun stopRecording() {
        AudioFeaturesAnalyzer.stopRecording()
        isRecording = false
        updateRecordButtonIdle()
    }

    private fun evaluateResult(rms: Double) {
        val earnedStar: Boolean
        val hint: String

        if (isTargetLoud) {
            earnedStar = rms > 1500.0 // arbitrary threshold for Loud
            hint = if (earnedStar) "Great! That was quite loud!" else "Too soft! Try shouting a bit more!"
        } else {
            earnedStar = rms in 100.0..800.0 // arbitrary threshold for Soft, but >100 to ensure they made a sound
            hint = when {
                rms < 100.0 -> "Too quiet! We didn't hear you."
                rms > 800.0 -> "Too loud! Try whispering!"
                else -> "Perfect soft voice!"
            }
        }

        tvStatus.text = hint

        val voiceResult = VoiceAnalyzer.VoiceResult(
            averageAmplitude = rms.toFloat(),
            maxAmplitude = rms.toFloat(),
            durationMs = 3000,
            spokenText = "Volume Practice",
            accuracyPercent = if (earnedStar) 100 else 0,
            starEarned = earnedStar,
            volumeLabel = if (isTargetLoud) "LOUD" else "SOFT",
            hint = hint
        )

        ResultDialog(this, voiceResult) {
            if (earnedStar) {
                val db = AppDatabase.getDatabase(this)
                val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
                val stars = prefs.getInt("total_stars", 0) + 1
                prefs.edit().putInt("total_stars", stars).apply()
            }
            setNewTarget()
        }.show()
    }

    private fun updateRecordButtonIdle() {
        btnRecord.setIconResource(android.R.drawable.ic_btn_speak_now)
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.coral))
        (btnRecord.tag as? android.animation.AnimatorSet)?.cancel()
        btnRecord.scaleX = 1f
        btnRecord.scaleY = 1f
    }

    private fun updateRecordButtonListening() {
        btnRecord.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
        val scaleX = android.animation.ObjectAnimator.ofFloat(btnRecord, "scaleX", 1f, 1.15f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(btnRecord, "scaleY", 1f, 1.15f, 1f)
        scaleX.repeatCount = android.animation.ObjectAnimator.INFINITE
        scaleY.repeatCount = android.animation.ObjectAnimator.INFINITE
        scaleX.duration = 800; scaleY.duration = 800
        val anim = android.animation.AnimatorSet()
        anim.playTogether(scaleX, scaleY)
        anim.start()
        btnRecord.tag = anim
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}

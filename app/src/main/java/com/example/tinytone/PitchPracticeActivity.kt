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

class PitchPracticeActivity : AppCompatActivity() {

    private lateinit var btnRecord: MaterialButton
    private lateinit var tvTargetPitch: TextView
    private lateinit var tvStatus: TextView
    private lateinit var waveformView: WaveformView

    private var isRecording = false
    private var isTargetHigh = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pitch_practice)

        btnRecord = findViewById(R.id.btnRecord)
        tvTargetPitch = findViewById(R.id.tvTargetPitch)
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
        isTargetHigh = Random.nextBoolean()
        tvTargetPitch.text = if (isTargetHigh) "HIGH PITCH" else "LOW PITCH"
        tvTargetPitch.setTextColor(ContextCompat.getColor(this, if (isTargetHigh) R.color.card_pitch else R.color.brand_primary))
        tvStatus.text = "Tap to practice your pitch control"
        updateRecordButtonIdle()
    }

    private fun startRecording() {
        isRecording = true
        waveformView.clear()
        tvStatus.text = "Recording 3 seconds..."
        updateRecordButtonListening()

        lifecycleScope.launch {
            val result = AudioFeaturesAnalyzer.recordAndAnalyze(3000L) { features ->
                // Draw waveform scale. Using ZCR / something for height, just for fun visual.
                // Alternatively, since wavefromView uses amplitude, we can map ZCR to amplitude visual.
                val amp = (features.zcr * 2f).coerceAtMost(200f)
                waveformView.addAmplitude(amp)
            }
            
            isRecording = false
            updateRecordButtonIdle()
            evaluateResult(result.zcr, result.rms)
        }
    }

    private fun stopRecording() {
        AudioFeaturesAnalyzer.stopRecording()
        isRecording = false
        updateRecordButtonIdle()
    }

    private fun evaluateResult(zcr: Int, rms: Double) {
        if (rms < 50.0) {
            tvStatus.text = "Too quiet to hear your pitch!"
            // Show failure dialog
            showResultDialog(false, "Too quiet to hear your pitch!", 0)
            return
        }

        val earnedStar: Boolean
        val hint: String

        // ZCR > 800 roughly means higher frequency components (squeaks, high pitch)
        // ZCR < 500 roughly means lower frequency (deep voice)
        // These are very rough heuristics for fun practice apps.
        if (isTargetHigh) {
            earnedStar = zcr > 600
            hint = if (earnedStar) "Great! Nice high pitch!" else "Too low! Try squeaking like a mouse!"
        } else {
            earnedStar = zcr < 600
            hint = if (earnedStar) "Good! That was a deep voice!" else "Too high! Try talking like a bear!"
        }

        tvStatus.text = hint

        val accuracy = if (earnedStar) 100 else 0
        showResultDialog(earnedStar, hint, accuracy)
    }

    private fun showResultDialog(earnedStar: Boolean, hint: String, accuracy: Int) {
        val voiceResult = VoiceAnalyzer.VoiceResult(
            averageAmplitude = 100f,
            maxAmplitude = 100f,
            durationMs = 3000,
            spokenText = "Pitch Practice",
            accuracyPercent = accuracy,
            starEarned = earnedStar,
            volumeLabel = if (isTargetHigh) "HIGH" else "LOW",
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

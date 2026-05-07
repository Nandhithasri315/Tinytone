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
    private lateinit var tvPrompt: TextView
    private lateinit var tvStatus: TextView
    private lateinit var waveformView: WaveformView

    private var isRecording  = false
    private var isTargetLoud = true

    // Practice prompts — child-friendly phrases
    private val loudPhrases = listOf(
        "Roar like a LION!", "Shout: HOORAY!", "Say it LOUD: HELLO!",
        "Yell: I AM BRAVE!", "Call out: GOOD MORNING WORLD!", "Say loudly: MY NAME IS..."
    )
    private val softPhrases = listOf(
        "Whisper: good night", "Say softly: shhh...", "Whisper: little bunny",
        "Say quietly: sweet dreams", "Whisper: la la la", "Murmur: hello moon"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loudness_practice)

        btnRecord       = findViewById(R.id.btnRecord)
        tvTargetVolume  = findViewById(R.id.tvTargetVolume)
        tvPrompt        = findViewById(R.id.tvPrompt)
        tvStatus        = findViewById(R.id.tvStatus)
        waveformView    = findViewById(R.id.waveformView)

        findViewById<AppCompatImageButton>(R.id.btnBack).setOnClickListener { finish() }

        setNewTarget()

        btnRecord.setOnClickListener {
            if (!isRecording) { if (checkPermission()) startRecording() }
            else stopRecording()
        }
    }

    private fun setNewTarget() {
        isTargetLoud = Random.nextBoolean()
        if (isTargetLoud) {
            tvTargetVolume.text = "🔊 LOUD!"
            tvTargetVolume.setTextColor(ContextCompat.getColor(this, R.color.card_loudness))
            tvPrompt.text = loudPhrases.random()
        } else {
            tvTargetVolume.text = "🤫 SOFT"
            tvTargetVolume.setTextColor(ContextCompat.getColor(this, R.color.sky_blue))
            tvPrompt.text = softPhrases.random()
        }
        tvStatus.text = "Tap the mic button and try!"
        updateRecordButtonIdle()
    }

    private fun startRecording() {
        isRecording = true
        waveformView.clear()
        tvStatus.text = if (isTargetLoud) "🔊 Go — be LOUD!" else "🤫 Now whisper…"
        updateRecordButtonListening()

        lifecycleScope.launch {
            val result = AudioFeaturesAnalyzer.recordAndAnalyze(3000L) { features ->
                // rms from AudioRecord: 0..32768 scale, speech typically 500-8000
                val amp = (features.rms / 30.0).toFloat().coerceIn(0f, 1000f)
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
        /*
         * AudioRecord PCM 16-bit RMS calibration (empirical, typical Android mic):
         *   Silence / background:  rms  <  200
         *   Whisper / very soft:   rms  200 – 1 500
         *   Normal speech:         rms 1 500 – 5 000
         *   Loud / shouting:       rms  > 5 000
         */
        val earnedStar: Boolean
        val hint: String

        if (isTargetLoud) {
            earnedStar = rms > 3000.0
            hint = when {
                rms < 200.0  -> "We didn't hear you at all! Speak up!"
                rms < 3000.0 -> "Good try — but go LOUDER! Roar!! (RMS: ${rms.toInt()})"
                else         -> "Amazing! That was super LOUD! 🔊"
            }
        } else {
            // Soft = anything below normal speech. Absolute silence (rms<200) is too quiet — they must make some sound.
            earnedStar = rms in 200.0..3000.0
            hint = when {
                rms < 200.0  -> "We couldn't hear you at all — whisper just a little louder!"
                rms > 3000.0 -> "Too loud! Try whispering softly 🤫 (RMS: ${rms.toInt()})"
                else         -> "Perfect soft voice! Great whispering! 🤫"
            }
        }

        tvStatus.text = hint

        val intent = android.content.Intent(this, ResultActivity::class.java).apply {
            putExtra("ACCURACY",    if (earnedStar) 100 else (rms / 80.0).toInt().coerceIn(0, 100))
            putExtra("IS_STAR",     earnedStar)
            putExtra("TARGET_WORD", if (isTargetLoud) "LOUD!" else "soft")
            putExtra("SPOKEN_WORD", hint)
            putExtra("FILE_PATH",   "")   // No playback in voice exercises
        }
        startActivity(intent)

        if (earnedStar) {
            val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
            prefs.edit().putInt("total_stars", prefs.getInt("total_stars", 0) + 1).apply()
        }
        setNewTarget()
    }

    private fun updateRecordButtonIdle() {
        btnRecord.setIconResource(android.R.drawable.ic_btn_speak_now)
        btnRecord.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.coral))
        (btnRecord.tag as? android.animation.AnimatorSet)?.cancel()
        btnRecord.scaleX = 1f; btnRecord.scaleY = 1f
    }

    private fun updateRecordButtonListening() {
        btnRecord.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
        btnRecord.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
        val sx = android.animation.ObjectAnimator.ofFloat(btnRecord, "scaleX", 1f, 1.15f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(btnRecord, "scaleY", 1f, 1.15f, 1f)
        sx.repeatCount = android.animation.ObjectAnimator.INFINITE; sx.duration = 800
        sy.repeatCount = android.animation.ObjectAnimator.INFINITE; sy.duration = 800
        val anim = android.animation.AnimatorSet()
        anim.playTogether(sx, sy); anim.start()
        btnRecord.tag = anim
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioFeaturesAnalyzer.stopRecording()
    }
}

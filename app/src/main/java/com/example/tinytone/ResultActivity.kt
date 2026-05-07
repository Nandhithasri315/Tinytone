package com.example.tinytone

import android.animation.ObjectAnimator
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tinytone.databinding.ActivityResultBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityResultBinding
    private lateinit var tts: TextToSpeech
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var soundManager: SoundManager
    
    private var targetWord = ""
    private var audioFilePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundManager = SoundManager(this)

        val accuracy = intent.getIntExtra("ACCURACY", 0)
        val isStar = intent.getBooleanExtra("IS_STAR", false)
        targetWord = intent.getStringExtra("TARGET_WORD") ?: ""
        val spokenWord = intent.getStringExtra("SPOKEN_WORD") ?: ""
        audioFilePath = intent.getStringExtra("FILE_PATH") ?: ""

        tts = TextToSpeech(this, this)

        binding.tvCorrectWord.text = targetWord
        binding.tvSpokenWord.text = spokenWord
        binding.tvAccuracyText.text = "$accuracy%"

        if (isStar) {
            binding.tvHeadline.text = "Awesome!"
            binding.tvHeadline.setTextColor(Color.parseColor("#4CAF50"))
            binding.tvEmoji.text = "🎉"
            binding.tvStarReward.visibility = View.VISIBLE
            soundManager.playCorrect()
            triggerConfetti()
        } else if (accuracy > 50) {
            binding.tvHeadline.text = "Good Try!"
            binding.tvHeadline.setTextColor(Color.parseColor("#FF9800"))
            binding.tvEmoji.text = "👍"
            binding.tvStarReward.visibility = View.GONE
            soundManager.playCorrect()
        } else {
            binding.tvHeadline.text = "Keep Practicing"
            binding.tvHeadline.setTextColor(Color.parseColor("#F44336"))
            binding.tvEmoji.text = "😅"
            binding.tvStarReward.visibility = View.GONE
            soundManager.playIncorrect()
        }

        animateEmoji()

        // Circular Indicator Animation
        binding.accuracyChart.max = 100
        ObjectAnimator.ofInt(binding.accuracyChart, "progress", 0, accuracy).apply {
            duration = 1000
            interpolator = OvershootInterpolator()
            start()
        }

        binding.btnPlayOriginal.setOnClickListener {
            soundManager.playClick()
            tts.speak(targetWord, TextToSpeech.QUEUE_FLUSH, null, "target")
        }

        // Hide playback button if there is no recording file (voice exercises skip recording)
        val recordingFile = java.io.File(audioFilePath)
        if (audioFilePath.isBlank() || !recordingFile.exists()) {
            binding.btnPlayRecording.visibility = android.view.View.GONE
        } else {
            binding.btnPlayRecording.setOnClickListener {
                soundManager.playClick()
                playUserRecording()
            }
        }

        binding.btnNext.setOnClickListener {
            soundManager.playClick()
            finish()
        }
    }

    private fun animateEmoji() {
        binding.tvEmoji.alpha = 0f
        binding.tvEmoji.scaleX = 0f
        binding.tvEmoji.scaleY = 0f
        binding.tvEmoji.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                binding.tvEmoji.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun triggerConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        binding.konfettiView.start(party)
    }

    private fun playUserRecording() {
        if (audioFilePath.isEmpty() || !File(audioFilePath).exists()) {
            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show()
            return
        }
        mediaPlayer?.release()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to play back recording", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        mediaPlayer?.release()
        mediaPlayer = null
        soundManager.release()
        super.onDestroy()
    }
}

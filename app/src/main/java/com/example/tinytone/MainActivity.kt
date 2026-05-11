package com.example.tinytone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tinytone.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Word Practice screen.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var soundManager: SoundManager

    private var isListening   = false
    private var recordingStart = 0L
    private var consecutiveStars = 0
    private var sessionStars     = 0
    private var totalWordsPlayed = 0
    private var currentCategory  = ""

    private val rmsHistory = mutableListOf<Float>()
    private val candidates  = mutableListOf<String>()
    private var selectedDifficulty = "EASY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundManager = SoundManager(this)

        // ViewModel setup
        val db   = AppDatabase.getDatabase(this)
        val repo = AppRepository(db.wordDao(), db.badgeDao(), db.sessionDao())
        viewModel = ViewModelProvider(this, ViewModelFactory(repo))[MainViewModel::class.java]

        // Difficulty and Category normalization
        selectedDifficulty = intent.getStringExtra("DIFFICULTY") ?: "EASY"
        currentCategory = intent.getStringExtra("CATEGORY") ?: ""

        // Fix: Ensure "Food" vs "Foods" naming consistency
        if (currentCategory.equals("Food", ignoreCase = true)) currentCategory = "Foods"

        binding.tvCategory.text =
            if (currentCategory.isNotEmpty()) "📂 $currentCategory" else "🎯 $selectedDifficulty"

        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()

        // Load Stars and Progress
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        consecutiveStars = prefs.getInt("consecutive_stars", 0)
        totalWordsPlayed = prefs.getInt("total_words", 0)
        val currentStars = prefs.getInt("total_stars", 0)
        binding.tvScore.text = "⭐ $currentStars"

        // Observe word changes
        viewModel.currentWord.observe(this) { entity ->
            if (entity == null) return@observe
            binding.tvTargetWord.text = entity.word.uppercase()
            binding.waveformView.clear()
            updateButtonIdle()
            
            // Animation for new word
            binding.tvTargetWord.alpha = 0f
            binding.tvTargetWord.scaleX = 0.5f
            binding.tvTargetWord.scaleY = 0.5f
            binding.tvTargetWord.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400).setInterpolator(OvershootInterpolator()).start()
        }

        // Database Repair
        lifecycleScope.launch {
            try {
                // Fix any singular "Food" categories in existing data
                db.openHelper.writableDatabase.execSQL(
                    "UPDATE words SET category = 'Foods' WHERE category = 'Food'"
                )
            } catch (e: Exception) { e.printStackTrace() }
            
            // Note: fetchNextWord is handled in onResume to avoid double calls on startup
        }

        binding.btnListen.setOnClickListener {
            soundManager.playClick()
            tts.speak(
                viewModel.currentWord.value?.word ?: "",
                TextToSpeech.QUEUE_FLUSH, null, "word"
            )
            animateButton(binding.btnListen)
        }

        binding.btnRecord.setOnClickListener {
            soundManager.playClick()
            if (!isListening) {
                if (checkMicPermission()) startListening()
            } else {
                stopListening()
            }
        }

        binding.btnSkip.setOnClickListener {
            soundManager.playClick()
            animateButton(binding.btnSkip)
            fetchNextWord()
        }

        binding.btnBack.setOnClickListener {
            soundManager.playClick()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchNextWord() {
        Log.d("TinyToneDebug", "Fetching word: Cat=$currentCategory, Diff=$selectedDifficulty")
        viewModel.fetchNextWord(
            isAdaptivePicker    = true,
            selectedDifficulty  = selectedDifficulty,
            challengeWordId     = null,
            category            = currentCategory
        )
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            binding.btnRecord.isEnabled = false
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_LONG).show()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { 
                isListening = true 
                Log.d("TinyToneDebug", "Recognizer: Ready")
            }
            override fun onBeginningOfSpeech() {
                Log.d("TinyToneDebug", "Recognizer: Beginning of speech")
            }
            override fun onRmsChanged(rmsdB: Float) {
                // Improved transformation: maps typical -2..10dB to approx 0..1700 range
                // Silence ≈ 0, Speech ≈ 500-1200, Loud ≈ 1500+
                val amp = ((rmsdB + 2f).coerceAtLeast(0f) * 140f)
                rmsHistory.add(amp)
                binding.waveformView.addAmplitude(amp)
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                updateButtonIdle()
                candidates.clear()
                val res = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                res?.let { 
                    candidates.addAll(it)
                    Log.d("TinyToneDebug", "Recognizer Results: $it")
                }
                processResult()
            }
            override fun onError(error: Int) {
                isListening = false
                updateButtonIdle()
                val errorMsg = when(error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions error"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech heard"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                    else -> "Error ($error)"
                }
                Log.e("TinyToneDebug", "Recognizer Error: $errorMsg")
                
                val target = viewModel.currentWord.value?.word ?: ""
                val result = VoiceAnalyzer.analyze(
                    rmsHistory, 
                    System.currentTimeMillis() - recordingStart, 
                    target, 
                    emptyList(), 
                    "Try again! ($errorMsg)"
                )
                showResult(result)
            }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                Log.d("TinyToneDebug", "Recognizer: End of speech")
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun startListening() {
        rmsHistory.clear(); candidates.clear()
        binding.waveformView.clear()
        recordingStart = System.currentTimeMillis()
        updateButtonListening()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer.startListening(intent)
        Log.d("TinyToneDebug", "Recognizer: Started Listening")
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
        updateButtonIdle()
    }

    private fun processResult() {
        val duration = System.currentTimeMillis() - recordingStart
        val target   = viewModel.currentWord.value?.word ?: ""
        val result   = VoiceAnalyzer.analyze(rmsHistory, duration, target, candidates)
        Log.d("TinyToneDebug", "Analysis: Acc=${result.accuracyPercent}%, Spoken=${result.spokenText}")
        showResult(result)
    }

    private fun showResult(result: VoiceAnalyzer.VoiceResult) {
        val wordId = viewModel.currentWord.value?.id ?: 0
        viewModel.submitScore(wordId, result.accuracyPercent, result.durationMs, selectedDifficulty)

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        totalWordsPlayed++
        
        var totalStars = prefs.getInt("total_stars", 0)
        if (result.starEarned) {
            totalStars++
            consecutiveStars++
            sessionStars++
        } else {
            consecutiveStars = 0
        }
        
        binding.tvScore.text = "⭐ $totalStars"

        // Update Progress and Stars
        prefs.edit()
            .putInt("total_stars", totalStars)
            .putInt("consecutive_stars", consecutiveStars)
            .putInt("total_words", totalWordsPlayed)
            .apply()

        // Award Badges
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            BadgeManager.checkAndAward(
                db.badgeDao(),
                totalStars,
                consecutiveStars,
                result.accuracyPercent,
                totalWordsPlayed,
                sessionStars
            )
        }

        startActivity(Intent(this, ResultActivity::class.java).apply {
            putExtra("ACCURACY",     result.accuracyPercent)
            putExtra("IS_STAR",      result.starEarned)
            putExtra("TARGET_WORD",  viewModel.currentWord.value?.word ?: "")
            putExtra("SPOKEN_WORD",  result.spokenText)
        })
    }

    private fun updateButtonIdle() {
        binding.btnRecord.setIconResource(android.R.drawable.ic_btn_speak_now)
        binding.btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.coral))
        (binding.btnRecord.tag as? android.animation.AnimatorSet)?.cancel()
        binding.btnRecord.scaleX = 1f; binding.btnRecord.scaleY = 1f
    }

    private fun updateButtonListening() {
        binding.btnRecord.setIconResource(android.R.drawable.ic_menu_close_clear_cancel)
        binding.btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
        val sx = android.animation.ObjectAnimator.ofFloat(binding.btnRecord, "scaleX", 1f, 1.15f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(binding.btnRecord, "scaleY", 1f, 1.15f, 1f)
        sx.repeatCount = android.animation.ObjectAnimator.INFINITE; sx.duration = 800
        sy.repeatCount = android.animation.ObjectAnimator.INFINITE; sy.duration = 800
        val anim = android.animation.AnimatorSet()
        anim.playTogether(sx, sy); anim.start()
        binding.btnRecord.tag = anim
    }

    private fun animateButton(v: android.view.View) {
        val sx = android.animation.ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.88f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.88f, 1f)
        android.animation.AnimatorSet().apply { playTogether(sx, sy); duration = 160; start() }
    }

    private fun checkMicPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return false
        }
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale.US
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val totalStars = prefs.getInt("total_stars", 0)
        binding.tvScore.text = "⭐ $totalStars"
        fetchNextWord()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
        soundManager.release()
        super.onDestroy()
    }
}

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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var waveformView: WaveformView
    private lateinit var btnListen: MaterialButton
    private lateinit var btnRecord: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var tvTargetWord: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvCategory: TextView
    private lateinit var scoreCard: MaterialCardView

    private lateinit var db: AppDatabase
    private lateinit var wordDao: WordDao
    private lateinit var badgeDao: BadgeDao
    private lateinit var speechRecognizer: SpeechRecognizer

    private var isRecording = false
    private var stars = 0
    private var recordingStartTime = 0L
    private var currentTargetWord = ""
    private var lastAccuracy = 0
    private var consecutiveStars = 0
    private var sessionStars = 0
    private var totalWordsPracticed = 0
    private var currentCategory = "All"
    private val rmsHistory = mutableListOf<Float>()
    private val candidates = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init UI — all IDs must exist in activity_main.xml
        waveformView  = findViewById(R.id.waveformView)
        btnListen     = findViewById(R.id.btnListen)
        btnRecord     = findViewById(R.id.btnRecord)
        btnSkip       = findViewById(R.id.btnSkip)
        tvTargetWord  = findViewById(R.id.tvTargetWord)
        tvScore       = findViewById(R.id.tvScore)
        tvCategory    = findViewById(R.id.tvCategory)
        scoreCard     = findViewById(R.id.scoreCard)

        // Receive category from intent
        currentCategory = intent.getStringExtra("CATEGORY") ?: "All"
        tvCategory.text = when (currentCategory) {
            "Animals" -> "🐾 Animals"
            "Foods"   -> "🍕 Foods"
            "Colors"  -> "🎨 Colors"
            "Body"    -> "🧍 Body"
            else      -> "🎯 All Words"
        }

        tts = TextToSpeech(this, this)

        // Load saved progress
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        stars               = prefs.getInt("total_stars", 0)
        totalWordsPracticed = prefs.getInt("total_words", 0)
        consecutiveStars    = prefs.getInt("consecutive_stars", 0)
        tvScore.text        = "⭐ $stars"

        db       = AppDatabase.getDatabase(this)
        wordDao  = db.wordDao()
        badgeDao = db.badgeDao()

        lifecycleScope.launch {
            if (wordDao.getWordCount() == 0) seedDatabase()
            BadgeManager.checkAndAward(badgeDao, stars, consecutiveStars, lastAccuracy, totalWordsPracticed, sessionStars)
            setRandomWord()
        }

        setupSpeechRecognizer()

        btnListen.setOnClickListener {
            tts.speak(currentTargetWord, TextToSpeech.QUEUE_FLUSH, null, "target_word")
            animateButton(btnListen)
        }

        btnRecord.setOnClickListener {
            if (!isRecording) {
                if (checkPermission()) startListening()
            } else {
                stopListening()
            }
        }

        btnSkip.setOnClickListener {
            animateButton(btnSkip)
            lifecycleScope.launch { setRandomWord() }
        }

        // Safe back button
        findViewById<AppCompatImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun animateButton(v: android.view.View) {
        val sx = android.animation.ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.88f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.88f, 1f)
        val a  = android.animation.AnimatorSet()
        a.playTogether(sx, sy)
        a.duration = 160
        a.start()
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            btnRecord.isEnabled = false
            Toast.makeText(this, "Speech recognition not available on this device.", Toast.LENGTH_LONG).show()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isRecording = true }
            override fun onBeginningOfSpeech() {}
            override fun onResults(results: Bundle?) {
                isRecording = false
                updateRecordButtonIdle()
                candidates.clear()
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { candidates.addAll(it) }
                processResult()
            }
            override fun onError(error: Int) {
                isRecording = false
                updateRecordButtonIdle()
                handleRecognitionError(error)
            }
            override fun onRmsChanged(rmsdB: Float) {
                val amp = (rmsdB + 10f).coerceAtLeast(0f) * 85f
                rmsHistory.add(amp)
                waveformView.addAmplitude(amp)
            }
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun startListening() {
        if (!::speechRecognizer.isInitialized) return
        rmsHistory.clear(); candidates.clear(); waveformView.clear()
        recordingStartTime = System.currentTimeMillis()
        updateRecordButtonListening()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer.startListening(intent)
    }

    private fun stopListening() {
        if (::speechRecognizer.isInitialized) speechRecognizer.stopListening()
        isRecording = false
        updateRecordButtonIdle()
    }

    private fun updateRecordButtonIdle() {
        btnRecord.text = ""
        btnRecord.setIconResource(android.R.drawable.ic_btn_speak_now)
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.coral))
        (btnRecord.tag as? android.animation.AnimatorSet)?.cancel()
        btnRecord.scaleX = 1f
        btnRecord.scaleY = 1f
    }

    private fun updateRecordButtonListening() {
        btnRecord.text = ""
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

    private fun handleRecognitionError(error: Int) {
        val hint = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Check your microphone."
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speak clearly and a bit louder."
            else -> "Try again! We missed that."
        }
        val result = VoiceAnalyzer.analyze(rmsHistory, System.currentTimeMillis() - recordingStartTime, currentTargetWord, emptyList(), hint)
        showResult(result)
    }

    private fun processResult() {
        val duration = System.currentTimeMillis() - recordingStartTime
        val result = VoiceAnalyzer.analyze(rmsHistory, duration, currentTargetWord, candidates)
        showResult(result)
    }

    private fun showResult(result: VoiceAnalyzer.VoiceResult) {
        lastAccuracy = result.accuracyPercent
        totalWordsPracticed++
        if (!result.starEarned) consecutiveStars = 0
        ResultDialog(this, result) {
            if (result.starEarned) awardStar() else {
                saveProgress()
                lifecycleScope.launch {
                    BadgeManager.checkAndAward(badgeDao, stars, consecutiveStars, lastAccuracy, totalWordsPracticed, sessionStars)
                }
            }
            lifecycleScope.launch { setRandomWord() }
        }.show()
    }

    private fun awardStar() {
        stars++; consecutiveStars++; sessionStars++
        tvScore.text = "⭐ $stars"
        // Safe animation on the scoreCard
        val sx = android.animation.ObjectAnimator.ofFloat(scoreCard, "scaleX", 1f, 1.3f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(scoreCard, "scaleY", 1f, 1.3f, 1f)
        val a  = android.animation.AnimatorSet()
        a.playTogether(sx, sy)
        a.duration = 400
        a.start()
        saveProgress()
        lifecycleScope.launch {
            BadgeManager.checkAndAward(badgeDao, stars, consecutiveStars, lastAccuracy, totalWordsPracticed, sessionStars)
        }
    }

    private fun saveProgress() {
        getSharedPreferences("TinyTone", MODE_PRIVATE).edit()
            .putInt("total_stars", stars)
            .putInt("total_words", totalWordsPracticed)
            .putInt("consecutive_stars", consecutiveStars)
            .apply()
    }

    private suspend fun seedDatabase() {
        val basicWords  = listOf("APPLE","BANANA","CHERRY","SUN","MOON","STAR","TREE","BALL","BOOK","CLOUD")
                          .map { WordEntity(word = it, category = "Basic") }
        val animals     = listOf("DOG","CAT","ELEPHANT","TIGER","LION","BEAR","MONKEY","RABBIT","BIRD","FISH","DEER","FOX")
                          .map { WordEntity(word = it, category = "Animals") }
        val foods       = listOf("PIZZA","BURGER","PASTA","RICE","BREAD","CHEESE","MILK","WATER","CAKE","CANDY","MANGO","LEMON")
                          .map { WordEntity(word = it, category = "Foods") }
        val colors      = listOf("RED","BLUE","GREEN","YELLOW","BLACK","WHITE","PURPLE","ORANGE","PINK","BROWN","GOLD","SILVER")
                          .map { WordEntity(word = it, category = "Colors") }
        val bodyParts   = listOf("HEAD","HAND","FOOT","EYE","EAR","NOSE","MOUTH","ARM","LEG","HAIR","KNEE","BACK")
                          .map { WordEntity(word = it, category = "Body") }
        wordDao.insertAll(basicWords + animals + foods + colors + bodyParts)
    }

    private fun setRandomWord() {
        lifecycleScope.launch {
            val word = if (currentCategory == "All") wordDao.getRandomWord()
                       else wordDao.getRandomWordByCategory(currentCategory)
            runOnUiThread {
                currentTargetWord = word?.word ?: "HELLO"
                tvTargetWord.text = currentTargetWord
                waveformView.clear()
                updateRecordButtonIdle()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return false
        }
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale.US
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
        super.onDestroy()
    }
}

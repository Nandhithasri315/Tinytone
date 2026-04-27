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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var waveformView: WaveformView
    private lateinit var btnListen: MaterialButton
    private lateinit var btnRecord: MaterialButton
    private lateinit var tvTargetWord: TextView
    private lateinit var tvScore: TextView

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
    private val rmsHistory = mutableListOf<Float>()
    private val candidates = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waveformView = findViewById(R.id.waveformView)
        btnListen = findViewById(R.id.btnListen)
        btnRecord = findViewById(R.id.btnRecord)
        tvTargetWord = findViewById(R.id.tvTargetWord)
        tvScore = findViewById(R.id.tvScore)

        tts = TextToSpeech(this, this)

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        stars = prefs.getInt("total_stars", 0)
        totalWordsPracticed = prefs.getInt("total_words", 0)
        consecutiveStars = prefs.getInt("consecutive_stars", 0)
        tvScore.text = "Stars: $stars"

        db = AppDatabase.getDatabase(this)
        wordDao = db.wordDao()
        badgeDao = db.badgeDao()

        lifecycleScope.launch {
            if (wordDao.getWordCount() == 0) seedDatabase()
            BadgeManager.checkAndAward(
                badgeDao,
                stars,
                consecutiveStars,
                lastAccuracy,
                totalWordsPracticed,
                sessionStars
            )
            setRandomWord()
        }

        setupSpeechRecognizer()

        btnListen.setOnClickListener {
            tts.speak(currentTargetWord, TextToSpeech.QUEUE_FLUSH, null, "target_word")
        }

        btnRecord.setOnClickListener {
            if (!isRecording) {
                if (checkPermission()) startListening()
            } else {
                stopListening()
            }
        }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            btnRecord.isEnabled = false
            btnRecord.text = "NOT READY"
            Toast.makeText(
                this,
                "Speech recognition is not available on this device.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                btnRecord.text = "LISTENING"
            }

            override fun onBeginningOfSpeech() {
                btnRecord.text = "GO!"
            }

            override fun onResults(results: Bundle?) {
                isRecording = false
                updateRecordButtonIdle()
                candidates.clear()
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.let { candidates.addAll(it) }
                processResult()
            }

            override fun onError(error: Int) {
                isRecording = false
                updateRecordButtonIdle()
                candidates.clear()
                handleRecognitionError(error)
            }

            override fun onRmsChanged(rmsdB: Float) {
                val amp = (rmsdB + 10f).coerceAtLeast(0f) * 80f
                rmsHistory.add(amp)
                waveformView.addAmplitude(amp)
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                btnRecord.text = "WAIT..."
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun startListening() {
        if (!::speechRecognizer.isInitialized) {
            Toast.makeText(this, "Microphone is not ready yet.", Toast.LENGTH_SHORT).show()
            return
        }

        rmsHistory.clear()
        candidates.clear()
        waveformView.clear()
        recordingStartTime = System.currentTimeMillis()
        isRecording = true
        updateRecordButtonListening()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        btnRecord.text = "DONE"
    }

    private fun updateRecordButtonIdle() {
        btnRecord.text = "TALK"
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.coral))
    }

    private fun updateRecordButtonListening() {
        btnRecord.text = "STOP"
        btnRecord.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_red))
    }

    private fun handleRecognitionError(error: Int) {
        val hint = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "We could not read the microphone. Please try again."
            SpeechRecognizer.ERROR_CLIENT -> "Listening stopped early. Tap the button and try again."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is needed to listen."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech service is busy. Try again in a moment."
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "We did not catch a clear word. Try speaking closer to the mic."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "The microphone is busy. Please wait a moment and try again."
            SpeechRecognizer.ERROR_SERVER ->
                "Speech service had a problem. Please try again."
            else -> "We could not understand that try. Please try again."
        }

        val result = VoiceAnalyzer.analyze(
            amplitudes = rmsHistory,
            durationMs = System.currentTimeMillis() - recordingStartTime,
            targetWord = currentTargetWord,
            candidates = emptyList(),
            forcedHint = hint
        )
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
            if (result.starEarned) {
                awardStar()
            } else {
                saveProgress()
            }
            setRandomWord()
        }.show()
    }

    private fun awardStar() {
        stars++
        consecutiveStars++
        sessionStars++
        tvScore.text = "Stars: $stars"
        saveProgress()
        lifecycleScope.launch {
            BadgeManager.checkAndAward(
                badgeDao,
                stars,
                consecutiveStars,
                lastAccuracy,
                totalWordsPracticed,
                sessionStars
            )
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
        val words = listOf(
            "APPLE", "BANANA", "CHERRY", "DRAGON", "EAGLE",
            "FROG", "GRAPES", "HAPPY", "HELLO", "ORANGE",
            "SUN", "MOON", "STAR", "TREE", "BIRD"
        ).map { WordEntity(word = it) }
        wordDao.insertAll(words)
    }

    private fun setRandomWord() {
        lifecycleScope.launch {
            val word = wordDao.getRandomWord()
            runOnUiThread {
                currentTargetWord = word?.word ?: "HELLO"
                tvTargetWord.text = currentTargetWord
                waveformView.clear()
                updateRecordButtonIdle()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                startListening()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is needed for speaking practice.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        super.onDestroy()
    }
}

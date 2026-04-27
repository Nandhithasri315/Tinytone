package com.example.tinytone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var waveformView: WaveformView
    private lateinit var btnStartStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvSentence: TextView
    private lateinit var speechRecognizer: SpeechRecognizer

    private var isRecording = false
    private var exerciseType = "soft"
    private val rmsHistory = mutableListOf<Float>()

    private val softSentences = listOf(
        "The tiny kitten sleeps softly",
        "Whisper a secret to a friend",
        "The baby is sleeping quietly",
        "Speak gently like the wind",
        "The mouse tiptoed silently"
    )
    private val loudSentences = listOf(
        "The lion roars loudly",
        "Cheer for your team",
        "Call out to your friend",
        "Shout with all your might",
        "The thunder booms loudly"
    )
    private val pitchSentences = listOf(
        "Start low then go high",
        "The rocket goes up up up",
        "From the valley to the mountain",
        "Begin slow then rise to the top",
        "The bird flies higher and higher"
    )

    private val rmsFirstHalf = mutableListOf<Float>()
    private val rmsSecondHalf = mutableListOf<Float>()
    private var halfReached = false
    private var totalSamples = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        exerciseType = intent.getStringExtra("TYPE") ?: "soft"
        val mascot = intent.getStringExtra("EMOJI") ?: "VOICE"
        val title = intent.getStringExtra("TITLE") ?: "Exercise"
        val instruction = intent.getStringExtra("INSTRUCTION") ?: ""

        waveformView = findViewById(R.id.exerciseWaveform)
        btnStartStop = findViewById(R.id.btnStartStop)
        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        tvSentence = findViewById(R.id.tvSentence)

        findViewById<TextView>(R.id.tvExerciseEmoji).text = mascot
        findViewById<TextView>(R.id.tvExerciseTitle).text = title
        findViewById<TextView>(R.id.tvInstruction).text = instruction

        showRandomSentence()
        setupRecognizer()
        updateIdleState()

        btnStartStop.setOnClickListener {
            if (!isRecording) {
                if (checkPermission()) startListening()
            } else {
                stopListening()
            }
        }
    }

    private fun setupRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            btnStartStop.isEnabled = false
            btnStartStop.text = "VOICE OFF"
            tvStatus.text = "Speech tools are not ready on this device."
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                tvStatus.text = "Listening now. Say the sentence."
            }

            override fun onBeginningOfSpeech() {
                tvStatus.text = "Nice. Keep going."
            }

            override fun onResults(results: Bundle?) {
                isRecording = false
                updateIdleState("Checking your voice...")
                val avg = if (rmsHistory.isEmpty()) 0f else rmsHistory.average().toFloat()
                evaluateExercise(avg)
                showRandomSentence()
            }

            override fun onError(error: Int) {
                isRecording = false
                updateIdleState(recognitionErrorMessage(error))
                tvResult.text = "Try again with a clear steady voice."
                tvResult.setTextColor(Color.parseColor("#F06292"))
                showRandomSentence()
            }

            override fun onRmsChanged(rmsdB: Float) {
                val amp = (rmsdB + 10f).coerceAtLeast(0f) * 80f
                rmsHistory.add(amp)
                waveformView.addAmplitude(amp)

                totalSamples++
                if (!halfReached) {
                    rmsFirstHalf.add(amp)
                    if (totalSamples == 180) halfReached = true
                } else {
                    rmsSecondHalf.add(amp)
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                tvStatus.text = "Almost done..."
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun startListening() {
        if (!::speechRecognizer.isInitialized) {
            Toast.makeText(this, "Speech recognizer is not ready.", Toast.LENGTH_SHORT).show()
            return
        }

        rmsHistory.clear()
        rmsFirstHalf.clear()
        rmsSecondHalf.clear()
        halfReached = false
        totalSamples = 0
        waveformView.clear()
        isRecording = true
        btnStartStop.text = "STOP"
        btnStartStop.setBackgroundColor(Color.parseColor("#EF5350"))
        tvStatus.text = "Listening now. Say the sentence."
        tvResult.text = ""

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        tvStatus.text = "Checking your voice..."
    }

    private fun updateIdleState(status: String = "Tap start and say the sentence") {
        btnStartStop.text = "START"
        btnStartStop.setBackgroundColor(Color.parseColor("#66BB6A"))
        tvStatus.text = status
    }

    private fun evaluateExercise(avgAmplitude: Float) {
        val passed: Boolean
        val message: String

        when (exerciseType) {
            "soft" -> {
                passed = avgAmplitude in 80f..600f
                message = when {
                    avgAmplitude < 80f -> "A little louder will help us hear you."
                    passed -> "Great quiet voice. That was gentle and clear."
                    else -> "That was strong. Try using a softer bedtime voice."
                }
            }
            "loud" -> {
                passed = avgAmplitude > 800f
                message = if (passed) {
                    "Great big voice. You sounded brave and clear."
                } else {
                    "Try a bigger playground voice."
                }
            }
            "pitch" -> {
                val avg1 = if (rmsFirstHalf.isEmpty()) 0f else rmsFirstHalf.average().toFloat()
                val avg2 = if (rmsSecondHalf.isEmpty()) 0f else rmsSecondHalf.average().toFloat()
                val variation = avg2 - avg1
                passed = variation > 50f && avgAmplitude > 80f
                message = if (passed) {
                    "Nice voice climb. We heard your pitch rise."
                } else {
                    "Start low and glide higher at the end."
                }
            }
            else -> {
                passed = false
                message = "Try that one more time."
            }
        }

        updateIdleState(if (passed) "Well done. Ready for another one?" else "Nice try. Let us do another one.")
        tvResult.text = message
        tvResult.setTextColor(
            if (passed) Color.parseColor("#2E7D32")
            else Color.parseColor("#F06292")
        )
    }

    private fun recognitionErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "We had trouble reading the microphone."
            SpeechRecognizer.ERROR_CLIENT -> "Listening stopped. Tap start to try again."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Please allow microphone access."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service is busy right now."
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "We did not catch that. Please say it clearly."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "The microphone is busy. Please wait a moment."
            SpeechRecognizer.ERROR_SERVER -> "Speech service had a problem."
            else -> "We could not hear a clear voice sample."
        }
    }

    private fun showRandomSentence() {
        val sentence = when (exerciseType) {
            "soft" -> softSentences.random()
            "loud" -> loudSentences.random()
            "pitch" -> pitchSentences.random()
            else -> "Say something fun"
        }
        tvSentence.text = sentence
    }

    private fun checkPermission(): Boolean {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
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
                Toast.makeText(this, "Microphone permission is needed for the exercise.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
        super.onDestroy()
    }
}

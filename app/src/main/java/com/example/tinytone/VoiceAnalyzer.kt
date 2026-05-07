package com.example.tinytone

object VoiceAnalyzer {

    data class VoiceResult(
        val averageAmplitude: Float,
        val maxAmplitude: Float,
        val durationMs: Long,
        val spokenText: String,
        val accuracyPercent: Int,
        val starEarned: Boolean,
        val volumeLabel: String,
        val hint: String
    )

    fun analyze(
        amplitudes: List<Float>,
        durationMs: Long,
        targetWord: String,
        candidates: List<String>,
        forcedHint: String? = null
    ): VoiceResult {

        val avg = if (amplitudes.isEmpty()) 0f else amplitudes.average().toFloat()
        val max = amplitudes.maxOrNull() ?: 0f

        // SpeechRecognizer onRmsChanged gives values typically in -2..10 dB
        // After our transform (rmsdB + 10f).coerceAtLeast(0f) * 85f
        // values cluster around  0-1700f  (silence ≈ 0, speech ≈ 500-1200)
        // We pick the best SPOKEN candidate by pronunciation similarity
        val best = candidates
            .filter { it.isNotBlank() }
            .maxByOrNull { PronunciationHelper.similarityScore(targetWord, it) }
            ?: ""

        val accuracy = if (best.isBlank()) {
            0
        } else {
            (PronunciationHelper.similarityScore(targetWord, best) * 100).toInt()
        }

        // Star: accuracy ≥ 75%, duration ≥ 200ms, some sound was heard (avg > 10f)
        val starEarned = accuracy >= 75 && durationMs >= 200 && avg > 10f

        val volumeLabel = when {
            avg < 50f  -> "Too quiet"
            avg > 1500f -> "Very loud"
            else       -> "Good speaking voice"
        }

        val defaultHint = when {
            best.isBlank() && avg < 30f -> "Move a little closer and say the word in a clear voice."
            best.isBlank()              -> "We heard your voice, but not the word clearly. Try once more."
            else                        -> PronunciationHelper.hint(targetWord, best)
        }

        return VoiceResult(
            averageAmplitude = avg,
            maxAmplitude = max,
            durationMs = durationMs,
            spokenText = if (best.isBlank()) "No clear word heard" else best,
            accuracyPercent = accuracy,
            starEarned = starEarned,
            volumeLabel = volumeLabel,
            hint = forcedHint ?: defaultHint
        )
    }
}

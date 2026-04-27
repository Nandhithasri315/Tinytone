package com.example.tinytone

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class ResultDialog(
    context: Context,
    private val result: VoiceAnalyzer.VoiceResult,
    private val onNext: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_result)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvEmoji = findViewById<TextView>(R.id.tvResultEmoji)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyPercent)
        val accuracyBar = findViewById<ProgressBar>(R.id.accuracyBar)
        val tvResultMsg = findViewById<TextView>(R.id.tvResultMessage)
        val tvStarMsg = findViewById<TextView>(R.id.tvStarMessage)
        val tvHint = findViewById<TextView>(R.id.tvHint)
        val btnAnalysis = findViewById<Button>(R.id.btnAnalysis)
        val analysisPanel = findViewById<LinearLayout>(R.id.analysisPanel)
        val tvHeard = findViewById<TextView>(R.id.tvHeard)
        val tvVolume = findViewById<TextView>(R.id.tvVolume)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val btnNext = findViewById<Button>(R.id.btnNext)

        val pct = result.accuracyPercent

        tvEmoji.text = when {
            pct >= 90 -> "SUPER"
            pct >= 75 -> "GREAT"
            pct >= 50 -> "TRY"
            else -> "AGAIN"
        }

        accuracyBar.progress = 0
        tvAccuracy.text = "0%"
        val handler = Handler(Looper.getMainLooper())
        var current = 0
        val runnable = object : Runnable {
            override fun run() {
                if (current <= pct) {
                    accuracyBar.progress = current
                    tvAccuracy.text = "$current%"
                    accuracyBar.progressTintList =
                        android.content.res.ColorStateList.valueOf(
                            when {
                                current >= 75 -> Color.parseColor("#66BB6A")
                                current >= 50 -> Color.parseColor("#FFCA28")
                                else -> Color.parseColor("#EF5350")
                            }
                        )
                    current++
                    handler.postDelayed(this, 15)
                }
            }
        }
        handler.post(runnable)

        tvResultMsg.text = when {
            pct >= 90 -> "Excellent speaking. You were very clear."
            pct >= 75 -> "Great job. You said it really well."
            pct >= 50 -> "Nice try. You are getting closer."
            else -> "Keep practicing. The next try can be better."
        }

        if (result.starEarned) {
            tvStarMsg.text = "Star earned"
            tvStarMsg.setTextColor(Color.parseColor("#F9A825"))
        } else {
            tvStarMsg.text = "Practice round"
            tvStarMsg.setTextColor(Color.parseColor("#EF5350"))
        }

        tvHint.text = result.hint

        btnAnalysis.setOnClickListener {
            if (analysisPanel.visibility == View.GONE) {
                analysisPanel.visibility = View.VISIBLE
                btnAnalysis.text = "Hide details"
            } else {
                analysisPanel.visibility = View.GONE
                btnAnalysis.text = "See details"
            }
        }

        tvHeard.text = "Heard: ${result.spokenText}"
        tvVolume.text = "Volume: ${result.volumeLabel}"
        tvDuration.text = "Time: ${"%.1f".format(result.durationMs / 1000.0)} sec"

        btnNext.setOnClickListener {
            dismiss()
            onNext()
        }
    }
}

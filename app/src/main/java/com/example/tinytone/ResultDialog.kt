package com.example.tinytone

import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class ResultDialog(
    context: Context,
    private val result: VoiceAnalyzer.VoiceResult,
    private val onNext: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_result)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Use correct MaterialButton type
        val tvEmoji       = findViewById<TextView>(R.id.tvResultEmoji)
        val tvAccuracy    = findViewById<TextView>(R.id.tvAccuracyPercent)
        val accuracyBar   = findViewById<ProgressBar>(R.id.accuracyBar)
        val tvResultMsg   = findViewById<TextView>(R.id.tvResultMessage)
        val tvStarMsg     = findViewById<TextView>(R.id.tvStarMessage)
        val tvHint        = findViewById<TextView>(R.id.tvHint)
        val btnAnalysis   = findViewById<MaterialButton>(R.id.btnAnalysis)
        val analysisPanel = findViewById<LinearLayout>(R.id.analysisPanel)
        val tvHeard       = findViewById<TextView>(R.id.tvHeard)
        val tvVolume      = findViewById<TextView>(R.id.tvVolume)
        val tvDuration    = findViewById<TextView>(R.id.tvDuration)
        val btnNext       = findViewById<MaterialButton>(R.id.btnNext)

        val pct = result.accuracyPercent

        // Header text
        tvEmoji.text = when {
            pct >= 90 -> "🎉 SUPERSTAR!"
            pct >= 75 -> "🌟 GREAT JOB!"
            pct >= 50 -> "💪 KEEP GOING!"
            else      -> "🔄 TRY AGAIN!"
        }

        // Animated progress bar
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
                                else          -> Color.parseColor("#EF5350")
                            }
                        )
                    current++
                    handler.postDelayed(this, 15)
                }
            }
        }
        handler.post(runnable)

        // Result message
        tvResultMsg.text = when {
            pct >= 90 -> "Excellent! You spoke very clearly."
            pct >= 75 -> "Great job! You're doing brilliantly."
            pct >= 50 -> "Nice try! You are getting closer."
            else      -> "Keep practicing. You'll get there!"
        }

        // Star message
        if (result.starEarned) {
            tvStarMsg.text = "⭐ Star Earned!"
            tvStarMsg.setTextColor(Color.parseColor("#F9A825"))
        } else {
            tvStarMsg.text = "Practice round — try again!"
            tvStarMsg.setTextColor(Color.parseColor("#FF7043"))
        }

        tvHint.text = result.hint

        // Details toggle
        btnAnalysis.setOnClickListener {
            if (analysisPanel.visibility == View.GONE) {
                analysisPanel.visibility = View.VISIBLE
                btnAnalysis.text = "Hide details"
            } else {
                analysisPanel.visibility = View.GONE
                btnAnalysis.text = "See details"
            }
        }

        tvHeard.text    = "🗣 Heard: ${result.spokenText}"
        tvVolume.text   = "🔊 Volume: ${result.volumeLabel}"
        tvDuration.text = "⏱ Time: ${"%.1f".format(result.durationMs / 1000.0)} sec"

        // Animate the dialog pop-in
        val dialogView = window?.decorView
        dialogView?.alpha = 0f
        dialogView?.scaleX = 0.8f
        dialogView?.scaleY = 0.8f
        val fadeIn = ObjectAnimator.ofFloat(dialogView, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(dialogView, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(dialogView, "scaleY", 0.8f, 1f)
        val anim = AnimatorSet()
        anim.playTogether(fadeIn, scaleX, scaleY)
        anim.duration = 300
        anim.start()

        btnNext.setOnClickListener {
            dismiss()
            onNext()
        }
    }
}

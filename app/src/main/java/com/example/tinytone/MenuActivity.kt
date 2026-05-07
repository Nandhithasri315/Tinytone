package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.card.MaterialCardView

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        refreshStars()
        refreshStreak()
        setupCards()
    }

    override fun onResume() {
        super.onResume()
        refreshStars()
        refreshStreak()
    }

    private fun refreshStars() {
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        findViewById<TextView>(R.id.tvMenuStars).text = "⭐ $stars stars earned"
    }

    private fun refreshStreak() {
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val streak = prefs.getInt("daily_streak", 0)
        val streakText = if (streak > 1) "🔥 $streak Day Streak! Keep it up!" else "🔥 Start your streak today!"
        // Only update if the view exists in layout
        try {
            findViewById<TextView>(R.id.tvDailyStreak)?.text = streakText
        } catch (e: Exception) { /* View might not exist */ }
    }

    private fun setupCards() {
        val opts = ActivityOptionsCompat.makeCustomAnimation(
            this, android.R.anim.fade_in, android.R.anim.fade_out)

        // 🎙️ Start Practice
        findViewById<MaterialCardView>(R.id.cardPractice)?.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, MainActivity::class.java), opts.toBundle())
            }
        }

        // 📢 Loudness Practice
        findViewById<MaterialCardView>(R.id.cardLoudness)?.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, LoudnessPracticeActivity::class.java), opts.toBundle())
            }
        }

        // 🎵 Pitch Practice
        findViewById<MaterialCardView>(R.id.cardPitch)?.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, PitchPracticeActivity::class.java), opts.toBundle())
            }
        }

        // 📚 Word Categories
        findViewById<MaterialCardView>(R.id.cardSettings)?.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, ExercisesActivity::class.java), opts.toBundle())
            }
        }

        // 📈 Progress
        findViewById<MaterialCardView>(R.id.cardProgress)?.setOnClickListener { v ->
            animateCard(v) {
                val intent = Intent(this, ProgressActivity::class.java)
                intent.putExtra("TAB", "progress")
                startActivity(intent, opts.toBundle())
            }
        }

        // 🏆 Badges
        findViewById<MaterialCardView>(R.id.cardBadges)?.setOnClickListener { v ->
            animateCard(v) {
                val intent = Intent(this, ProgressActivity::class.java)
                intent.putExtra("TAB", "badges")
                startActivity(intent, opts.toBundle())
            }
        }
    }

    private fun animateCard(v: android.view.View, action: () -> Unit) {
        val scaleX = android.animation.ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.95f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.95f, 1f)
        val animSet = android.animation.AnimatorSet()
        animSet.playTogether(scaleX, scaleY)
        animSet.duration = 150
        animSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                action()
            }
        })
        animSet.start()
    }
}

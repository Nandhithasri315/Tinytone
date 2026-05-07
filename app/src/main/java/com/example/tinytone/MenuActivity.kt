package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.card.MaterialCardView

import com.example.tinytone.databinding.ActivityMenuBinding
import java.util.Calendar

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshStars()
        updateAndDisplayStreak()
        setupCards()
    }

    override fun onResume() {
        super.onResume()
        refreshStars()
        updateAndDisplayStreak()
    }

    private fun refreshStars() {
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        binding.tvMenuStars.text = "⭐ $stars stars earned"
    }

    private fun updateAndDisplayStreak() {
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val lastVisit = prefs.getLong("last_visit", 0L)
        var streak = prefs.getInt("daily_streak", 0)
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (lastVisit > 0) {
            val diff = today - lastVisit
            val oneDay = 24 * 60 * 60 * 1000L
            streak = when {
                diff == 0L -> streak // Same day
                diff == oneDay -> streak + 1 // Consecutive day
                else -> 1 // Missed a day
            }
        } else {
            streak = 1
        }
        
        prefs.edit().putLong("last_visit", today).putInt("daily_streak", streak).apply()

        val streakText = if (streak > 1) "🔥 $streak Day Streak! Keep it up!" else "🔥 Start your streak today!"
        binding.tvDailyStreak.text = streakText
    }

    private fun getSelectedDifficulty(): String {
        return when (binding.toggleDifficulty.checkedButtonId) {
            R.id.btnEasy -> "EASY"
            R.id.btnMedium -> "MEDIUM"
            R.id.btnHard -> "HARD"
            else -> "EASY"
        }
    }

    private fun setupCards() {
        val opts = ActivityOptionsCompat.makeCustomAnimation(
            this, android.R.anim.fade_in, android.R.anim.fade_out)

        binding.cardPractice.setOnClickListener { v ->
            animateCard(v) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("DIFFICULTY", getSelectedDifficulty())
                }
                startActivity(intent, opts.toBundle())
            }
        }

        binding.cardLoudness.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, LoudnessPracticeActivity::class.java), opts.toBundle())
            }
        }

        binding.cardPitch.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, PitchPracticeActivity::class.java), opts.toBundle())
            }
        }

        binding.cardSettings.setOnClickListener { v ->
            animateCard(v) {
                startActivity(Intent(this, ExercisesActivity::class.java), opts.toBundle())
            }
        }

        binding.cardProgress.setOnClickListener { v ->
            animateCard(v) {
                val intent = Intent(this, ProgressActivity::class.java).apply {
                    putExtra("TAB", "progress")
                }
                startActivity(intent, opts.toBundle())
            }
        }

        binding.cardProgress.setOnLongClickListener { 
            // Launch Parent Dashboard
            val intent = Intent(this, ParentDashboardActivity::class.java)
            startActivity(intent, opts.toBundle())
            true
        }

        binding.cardBadges.setOnClickListener { v ->
            animateCard(v) {
                val intent = Intent(this, ProgressActivity::class.java).apply {
                    putExtra("TAB", "badges")
                }
                startActivity(intent, opts.toBundle())
            }
        }
    }

    private fun animateCard(v: android.view.View, action: () -> Unit) {
        val scaleX = android.animation.ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.95f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.95f, 1f)
        val animSet = android.animation.AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 150
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    action()
                }
            })
        }
        animSet.start()
    }
}

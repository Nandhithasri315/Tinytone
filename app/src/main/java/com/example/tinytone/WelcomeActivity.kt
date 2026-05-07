package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("is_first_time", true)
        
        if (isFirstTime) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_welcome)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvStreakCount = findViewById<TextView>(R.id.tvStreakCount)
        val tvMascot = findViewById<TextView>(R.id.tvMascotWelcome)
        val btnStart = findViewById<MaterialButton>(R.id.btnStartLearning)
        val tvWelcomeStars = findViewById<TextView>(R.id.tvWelcomeStars)
        val tvWelcomeWords = findViewById<TextView>(R.id.tvWelcomeWords)

        // Load stats
        val stars = prefs.getInt("total_stars", 0)
        val words = prefs.getInt("total_words", 0)
        tvWelcomeStars.text = "⭐ $stars"
        tvWelcomeWords.text = "📝 $words"

        // Dynamic Greeting based on time
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning, Hero! ☀️"
            in 12..16 -> "Good Afternoon, Champ! 🌤️"
            else -> "Good Evening, Legend! 🌙"
        }

        // Streak Logic
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
                diff == 0L -> streak // Same day, no change
                diff == oneDay -> streak + 1 // Next day, increment
                else -> 1 // Missed a day, reset
            }
        } else {
            streak = 1
        }
        
        prefs.edit().putLong("last_visit", today).putInt("daily_streak", streak).apply()
        tvStreakCount.text = "\uD83D\uDD25 $streak Day Streak!"

        // Animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        tvGreeting.startAnimation(fadeIn)
        tvMascot.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))

        btnStart.setOnClickListener {
            val scaleDown = android.animation.AnimatorSet()
            val sx = android.animation.ObjectAnimator.ofFloat(btnStart, "scaleX", 1f, 0.95f, 1f)
            val sy = android.animation.ObjectAnimator.ofFloat(btnStart, "scaleY", 1f, 0.95f, 1f)
            scaleDown.playTogether(sx, sy)
            scaleDown.duration = 200
            scaleDown.start()

            btnStart.postDelayed({
                val opts = androidx.core.app.ActivityOptionsCompat.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out)
                startActivity(
                    Intent(this, MenuActivity::class.java),
                    opts.toBundle()
                )
            }, 200)
        }
    }
}

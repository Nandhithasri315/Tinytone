package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        findViewById<TextView>(R.id.tvMenuStars).text = "$stars stars earned"

        findViewById<CardView>(R.id.cardPractice).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<CardView>(R.id.cardBadges).setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            intent.putExtra("TAB", "badges")
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardProgress).setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            intent.putExtra("TAB", "progress")
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardSettings).setOnClickListener {
            startActivity(Intent(this, ExercisesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        findViewById<TextView>(R.id.tvMenuStars).text = "$stars stars earned"
    }
}

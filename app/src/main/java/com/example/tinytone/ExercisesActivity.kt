package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ExercisesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnBack)
            .setOnClickListener { finish() }

        findViewById<CardView>(R.id.cardSoft).setOnClickListener {
            openExercise("soft", "SOFT", "Speak Softly", "Use your quiet bedtime voice.")
        }

        findViewById<CardView>(R.id.cardLoud).setOnClickListener {
            openExercise("loud", "LOUD", "Speak Loudly", "Use your big outdoor voice.")
        }

        findViewById<CardView>(R.id.cardPitch).setOnClickListener {
            openExercise("pitch", "PITCH", "Pitch Adventure", "Start low and glide higher.")
        }
    }

    private fun openExercise(type: String, emoji: String, title: String, instruction: String) {
        val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra("TYPE", type)
            putExtra("EMOJI", emoji)
            putExtra("TITLE", title)
            putExtra("INSTRUCTION", instruction)
        }
        startActivity(intent)
    }
}

package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.material.card.MaterialCardView

class ExercisesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        // Safe back button
        findViewById<AppCompatImageButton>(R.id.btnBack)
            .setOnClickListener { finish() }

        // Cards are now MaterialCardView in the updated layout
        listOf(
            R.id.cardSoft  to "Animals",
            R.id.cardLoud  to "Foods",
            R.id.cardPitch to "Colors"
        ).forEach { (id, category) ->
            findViewById<MaterialCardView>(id).setOnClickListener {
                animateCard(it) { openCategory(category) }
            }
        }
    }

    private fun animateCard(v: android.view.View, action: () -> Unit) {
        val sx = android.animation.ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.93f, 1f)
        val sy = android.animation.ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.93f, 1f)
        val a  = android.animation.AnimatorSet()
        a.playTogether(sx, sy)
        a.duration = 160
        a.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) { action() }
        })
        a.start()
    }

    private fun openCategory(category: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("CATEGORY", category)
        })
    }
}

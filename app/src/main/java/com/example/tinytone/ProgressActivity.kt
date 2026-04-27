package com.example.tinytone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ProgressActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var badgeDao: BadgeDao
    private lateinit var tvTotalStars: TextView
    private lateinit var tvTotalWords: TextView
    private lateinit var tvEarnedBadges: TextView
    private lateinit var rvBadges: RecyclerView
    // Add these two new variables at the top
    private lateinit var tvPageTitle: TextView
    private lateinit var statsCard: androidx.cardview.widget.CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        val tab = intent.getStringExtra("TAB") ?: "progress"

        tvTotalStars   = findViewById(R.id.tvTotalStars)
        tvTotalWords   = findViewById(R.id.tvTotalWords)
        tvEarnedBadges = findViewById(R.id.tvEarnedBadges)
        rvBadges       = findViewById(R.id.rvBadges)

        db       = AppDatabase.getDatabase(this)
        badgeDao = db.badgeDao()

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        val words = prefs.getInt("total_words", 0)

        // Fix 1 — show correct content based on tab
        tvPageTitle = findViewById(R.id.tvPageTitle)
        statsCard   = findViewById(R.id.statsCard)

        if (tab == "badges") {
            // Badges tab — hide stats card, change title
            statsCard.visibility   = View.GONE
            tvPageTitle.text       = "My Badges 🏅"
        } else {
            // Progress tab — show stats card
            statsCard.visibility   = View.VISIBLE
            tvPageTitle.text       = "My Progress 📊"
            tvTotalStars.text      = "⭐ $stars Stars Earned"
            tvTotalWords.text      = "📚 $words Words Practiced"
        }

        lifecycleScope.launch {
            // Seed badges if table is empty
            if (badgeDao.getAllBadges().isEmpty()) {
                BadgeManager.checkAndAward(
                    badgeDao, stars, 0, 0, words, 0
                )
            }

            // Fix 2 — filter out any empty/null badges before showing
            val allBadges = badgeDao.getAllBadges()
                .filter { it.id.isNotBlank() && it.title.isNotBlank() }

            val earnedCount = allBadges.count { it.earned }

            runOnUiThread {
                if (tab == "progress") {
                    tvEarnedBadges.text = "🏅 $earnedCount / ${allBadges.size} Badges Earned"
                }
                rvBadges.layoutManager = GridLayoutManager(this@ProgressActivity, 2)
                rvBadges.adapter = BadgeAdapter(allBadges)
            }
        }

        findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnBack)
            .setOnClickListener { finish() }
    }
}
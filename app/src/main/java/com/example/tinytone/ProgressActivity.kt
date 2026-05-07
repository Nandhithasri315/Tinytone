package com.example.tinytone

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
    private lateinit var tvPageTitle: TextView
    private lateinit var statsContainer: LinearLayout // Changed from CardView to LinearLayout to fix crash
    private lateinit var tvBadgesTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        val tab = intent.getStringExtra("TAB") ?: "progress"

        // Initialize Views
        tvTotalStars   = findViewById(R.id.tvTotalStars)
        tvTotalWords   = findViewById(R.id.tvTotalWords)
        tvEarnedBadges = findViewById(R.id.tvEarnedBadges)
        rvBadges       = findViewById(R.id.rvBadges)
        tvPageTitle    = findViewById(R.id.tvPageTitle)
        statsContainer = findViewById(R.id.statsCard)
        tvBadgesTitle  = findViewById(R.id.tvBadgesTitle)

        db       = AppDatabase.getDatabase(this)
        badgeDao = db.badgeDao()

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        val words = prefs.getInt("total_words", 0)

        // UI Setup based on selected tab
        if (tab == "badges") {
            statsContainer.visibility = View.GONE
            tvPageTitle.text = "Achievements 🏆"
            tvBadgesTitle.visibility = View.GONE
        } else {
            statsContainer.visibility = View.VISIBLE
            tvPageTitle.text = "Your Journey 🚀"
            tvTotalStars.text = "⭐ $stars"
            tvTotalWords.text = "📝 $words"
            tvBadgesTitle.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            // Ensure badges are initialized
            val currentBadges = badgeDao.getAllBadges()
            if (currentBadges.isEmpty()) {
                BadgeManager.checkAndAward(badgeDao, stars, 0, 0, words, 0)
            }

            val allBadges = badgeDao.getAllBadges()
                .filter { it.id.isNotBlank() && it.title.isNotBlank() }
            
            val earnedCount = allBadges.count { it.earned }

            runOnUiThread {
                tvEarnedBadges.text = "🏅 $earnedCount"
                
                rvBadges.layoutManager = GridLayoutManager(this@ProgressActivity, 2)
                rvBadges.adapter = BadgeAdapter(allBadges)
                rvBadges.setHasFixedSize(true)
            }
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { 
            onBackPressedDispatcher.onBackPressed() 
        }
    }
}

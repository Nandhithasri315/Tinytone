package com.example.tinytone

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressActivity : AppCompatActivity() {

    private lateinit var viewModel: ProgressViewModel
    private lateinit var tvTotalStars: TextView
    private lateinit var tvTotalWords: TextView
    private lateinit var tvEarnedBadges: TextView
    private lateinit var rvBadges: RecyclerView
    private lateinit var tvPageTitle: TextView
    private lateinit var statsContainer: LinearLayout
    private lateinit var tvBadgesTitle: TextView
    private lateinit var weeklyBarContainer: LinearLayout
    private lateinit var chartCard: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Init database repository and ViewModel
        val db = AppDatabase.getDatabase(this)
        val repository = AppRepository(db.wordDao(), db.badgeDao(), db.sessionDao())
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[ProgressViewModel::class.java]

        val tab = intent.getStringExtra("TAB") ?: "progress"

        // Initialize Views
        tvTotalStars   = findViewById(R.id.tvTotalStars)
        tvTotalWords   = findViewById(R.id.tvTotalWords)
        tvEarnedBadges = findViewById(R.id.tvEarnedBadges)
        rvBadges       = findViewById(R.id.rvBadges)
        tvPageTitle    = findViewById(R.id.tvPageTitle)
        statsContainer = findViewById(R.id.statsCard)
        tvBadgesTitle  = findViewById(R.id.tvBadgesTitle)
        weeklyBarContainer = findViewById(R.id.weeklyBarContainer)
        chartCard      = findViewById(R.id.chartCard)

        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val stars = prefs.getInt("total_stars", 0)
        val words = prefs.getInt("total_words", 0)

        // UI Setup based on selected tab
        if (tab == "badges") {
            statsContainer.visibility = View.GONE
            chartCard.visibility = View.GONE
            tvPageTitle.text = "Achievements 🏆"
            tvBadgesTitle.visibility = View.GONE
        } else {
            statsContainer.visibility = View.VISIBLE
            chartCard.visibility = View.VISIBLE
            tvPageTitle.text = "Your Journey 🚀"
            tvTotalStars.text = "⭐ $stars"
            tvTotalWords.text = "📝 $words"
            tvBadgesTitle.visibility = View.VISIBLE
        }

        // Observers
        viewModel.badges.observe(this) { allBadges ->
            if (allBadges.isNotEmpty()) {
                val earnedCount = allBadges.count { it.earned }
                tvEarnedBadges.text = "🏅 $earnedCount"

                rvBadges.layoutManager = GridLayoutManager(this, 2)
                rvBadges.adapter = BadgeAdapter(allBadges.filter { it.id.isNotBlank() })
                rvBadges.setHasFixedSize(true)
            }
        }

        viewModel.recentSessions.observe(this) { sessions ->
            populateChartData(sessions)
        }

        // Trigger load
        viewModel.loadProgressData()

        findViewById<View>(R.id.btnBack).setOnClickListener { 
            onBackPressedDispatcher.onBackPressed() 
        }
    }

    private fun populateChartData(sessions: List<SessionEntity>) {
        weeklyBarContainer.removeAllViews()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        
        var maxMinutes = 1f
        val dailyMinutes = FloatArray(7)
        val labels = Array(7) { "" }

        for (i in 6 downTo 0) {
            val startOfDay = now - (i * dayMs)
            val endOfDay = startOfDay + dayMs
            
            val durationMs = sessions
                .filter { it.timestamp in startOfDay..endOfDay }
                .sumOf { it.durationMs }
                
            val minutes = durationMs / 1000f / 60f
            dailyMinutes[6 - i] = minutes
            labels[6 - i] = sdf.format(Date(startOfDay))
            if (minutes > maxMinutes) maxMinutes = minutes
        }

        for (i in 0..6) {
            val minutes = dailyMinutes[i]
            val heightWeight = (minutes / maxMinutes).coerceAtLeast(0.05f)

            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            val bar = View(this).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#00BBF9"))
                    cornerRadius = 12f
                }
                layoutParams = LinearLayout.LayoutParams(40, 0, heightWeight).apply {
                    bottomMargin = 16
                }
            }

            val label = TextView(this).apply {
                text = labels[i]
                textSize = 11f
                setTextColor(Color.parseColor("#757575"))
                gravity = Gravity.CENTER
            }

            column.addView(bar)
            column.addView(label)
            weeklyBarContainer.addView(column)
        }
    }
}

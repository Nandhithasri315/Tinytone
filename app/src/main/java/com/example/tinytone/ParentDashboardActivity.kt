package com.example.tinytone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tinytone.databinding.ActivityParentDashboardBinding

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AppRepository(
            AppDatabase.getDatabase(this).wordDao(),
            AppDatabase.getDatabase(this).badgeDao(),
            AppDatabase.getDatabase(this).sessionDao()
        )
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MenuViewModel::class.java]

        setupPinLogic()
        observeViewModel()
    }

    private fun setupPinLogic() {
        val prefs = getSharedPreferences("TinyTone", MODE_PRIVATE)
        val savedPin = prefs.getString("parent_pin", "1234") // Default PIN

        binding.btnSubmitPin.setOnClickListener {
            val entered = binding.etPin.text.toString()
            if (entered == savedPin) {
                binding.layoutPin.visibility = View.GONE
                binding.layoutDashboard.visibility = View.VISIBLE
                viewModel.loadParentDashboardStats()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                binding.etPin.text.clear()
            }
        }

        binding.btnResetProgress.setOnClickListener {
            // Provide visual feedback for reset logic
            // To be implemented fully: Wipe DB, reset SharedPreferences
            prefs.edit().clear().apply()
            Toast.makeText(this, "Progress Reset (Mocked completely next launch)!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.totalSessions.observe(this) { count ->
            binding.tvTotalSessions.text = "Total Sessions: $count"
        }
        viewModel.timeSpent.observe(this) { ms ->
            val minutes = ms / 1000 / 60
            binding.tvTotalTime.text = "Time Spent: $minutes mins"
        }
        viewModel.avgAccuracyEasy.observe(this) { acc ->
            binding.tvAccuracyEasy.text = "EASY: ${acc?.toInt() ?: "--"}%"
        }
        viewModel.avgAccuracyMedium.observe(this) { acc ->
            binding.tvAccuracyMedium.text = "MEDIUM: ${acc?.toInt() ?: "--"}%"
        }
        viewModel.avgAccuracyHard.observe(this) { acc ->
            binding.tvAccuracyHard.text = "HARD: ${acc?.toInt() ?: "--"}%"
        }
    }
}

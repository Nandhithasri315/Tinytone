package com.example.tinytone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tinytone.databinding.ActivityOnboardingBinding

data class OnboardingItem(val title: String, val description: String, val emoji: String)

class OnboardingAdapter(private val items: List<OnboardingItem>) : 
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvOnboardingEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvOnboardingTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvOnboardingDesc)
        val lottieView: View = view.findViewById(R.id.lottieAnimation) // Lottie reference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        
        holder.lottieView.visibility = View.GONE
        holder.tvEmoji.visibility = View.VISIBLE
        holder.tvEmoji.text = item.emoji
    }

    override fun getItemCount() = items.size
}

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var indicators: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf(
            OnboardingItem("Welcome to TinyTone", "Learn to speak with confidence using interactive voice games!", "🎙️"),
            OnboardingItem("Track Progress", "Earn stars, unlock achievement badges, and build your daily streak.", "⭐"),
            OnboardingItem("For Parents Too", "View detailed learning analytics safely hidden in the parent dashboard.", "📊")
        )

        binding.viewPager.adapter = OnboardingAdapter(items)
        setupIndicators(items.size)
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                if (position == items.size - 1) {
                    binding.btnNext.text = "GET STARTED"
                } else {
                    binding.btnNext.text = "NEXT"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < items.size) {
                binding.viewPager.currentItem += 1
            } else {
                getSharedPreferences("TinyTone", MODE_PRIVATE).edit()
                    .putBoolean("is_first_time", false)
                    .apply()
                
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private fun setupIndicators(count: Int) {
        indicators = Array(count) { ImageView(this) }
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(8, 0, 8, 0) }

        for (i in indicators.indices) {
            indicators[i].setImageDrawable(
                ContextCompat.getDrawable(applicationContext, android.R.drawable.presence_invisible)
            )
            // Use simple circle dots manually using basic shapes or custom drawables.
            // Using a colored dot directly by background instead
            val dot = View(this).apply {
                background = ContextCompat.getDrawable(this@OnboardingActivity, R.drawable.rounded_gray_pill)
                this.layoutParams = LinearLayout.LayoutParams(24, 24).apply { setMargins(8, 0, 8, 0) }
            }
            binding.indicators.addView(dot)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicators.childCount
        for (i in 0 until childCount) {
            val dot = binding.indicators.getChildAt(i)
            if (i == index) {
                dot.backgroundTintList = ContextCompat.getColorStateList(this, R.color.brand_primary)
                dot.layoutParams = LinearLayout.LayoutParams(64, 24).apply { setMargins(8, 0, 8, 0) }
            } else {
                dot.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                dot.layoutParams = LinearLayout.LayoutParams(24, 24).apply { setMargins(8, 0, 8, 0) }
            }
        }
    }
}

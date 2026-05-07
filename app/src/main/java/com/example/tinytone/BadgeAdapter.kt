package com.example.tinytone

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class BadgeAdapter(private val badges: List<BadgeEntity>) :
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView       = view.findViewById(R.id.tvBadgeEmoji)
        val tvTitle: TextView       = view.findViewById(R.id.tvBadgeTitle)
        val tvDesc: TextView        = view.findViewById(R.id.tvBadgeDesc)
        val tvStatus: TextView      = view.findViewById(R.id.tvBadgeStatus)
        val bgCircle: View          = view.findViewById(R.id.badgeBgCircle)
        val card: CardView          = view.findViewById(R.id.badgeCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        
        holder.tvEmoji.text = badge.emoji
        holder.tvTitle.text = badge.title
        holder.tvDesc.text  = badge.description

        if (badge.earned) {
            holder.tvStatus.text = "EARNED"
            holder.tvStatus.setTextColor(Color.WHITE)
            holder.tvStatus.setBackgroundResource(R.drawable.rounded_green_pill)
            
            holder.bgCircle.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            holder.bgCircle.alpha = 1.0f
            holder.card.alpha = 1.0f
            holder.card.cardElevation = 8f
            holder.tvEmoji.paint.colorFilter = null

            // 3D Flip animation on click
            holder.card.setOnClickListener {
                val anim = ObjectAnimator.ofFloat(holder.card, "rotationY", 0f, 360f)
                anim.duration = 800
                anim.start()
            }
        } else {
            holder.tvStatus.text = "LOCKED"
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"))
            holder.tvStatus.setBackgroundResource(R.drawable.rounded_gray_pill)
            
            holder.bgCircle.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            holder.bgCircle.alpha = 0.3f
            holder.card.alpha = 0.6f
            holder.card.cardElevation = 2f
            
            // Grayscale emoji
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            holder.tvEmoji.paint.colorFilter = ColorMatrixColorFilter(matrix)

            holder.card.setOnClickListener {
                val anim = ObjectAnimator.ofFloat(holder.card, "rotationY", 0f, 20f, -20f, 0f)
                anim.duration = 300
                anim.start()
            }
        }
    }

    override fun getItemCount() = badges.size
}

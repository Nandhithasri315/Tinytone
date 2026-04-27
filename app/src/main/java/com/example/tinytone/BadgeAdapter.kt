package com.example.tinytone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BadgeAdapter(private val badges: List<BadgeEntity>) :
    RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView       = view.findViewById(R.id.tvBadgeEmoji)
        val tvTitle: TextView       = view.findViewById(R.id.tvBadgeTitle)
        val tvDesc: TextView        = view.findViewById(R.id.tvBadgeDesc)
        val tvStatus: TextView      = view.findViewById(R.id.tvBadgeStatus)
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
            holder.tvStatus.text = "✅ Earned!"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            holder.itemView.alpha = 1.0f
        } else {
            holder.tvStatus.text = "🔒 Locked"
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"))
            holder.itemView.alpha = 0.5f
        }
    }

    override fun getItemCount() = badges.size
}
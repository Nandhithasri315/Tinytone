package com.example.tinytone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val id: String,        // e.g. "first_star"
    val title: String,     // e.g. "First Star"
    val emoji: String,     // e.g. "🌟"
    val description: String,
    val earned: Boolean = false
)